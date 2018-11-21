import {Injectable} from '@angular/core';
import {Observable, Subject, timer} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {EventBusService} from './event-bus.service';
import {Message, Participation, Room} from '../model';
import {
  CreateRoomRequest,
  DeleteRoomRequest,
  JoinRoomRequest,
  NotifyWritingUserRequest,
  SendMessageRequest
} from '../requests';
import {AuthService} from './auth.service';
import {map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private static API_PREFIX = '/api';

  private static EVENTS = ChatService.API_PREFIX + '/events';

  private static ROOMS = ChatService.API_PREFIX + '/rooms';

  private static USERS = ChatService.API_PREFIX + '/users';

  private static ROOM_DELETED = 'rooms.deleted';
  private static ROOM_JOINED = 'rooms.joined';
  private static MESSAGE_SENT = 'messages.sent';
  private static ROOM_CREATED = 'rooms.created';
  private static USER_TYPING = 'users.typing';
  private static ROOM_LEFT = 'rooms.left';
  private static userTypingAddress(room: string) {
    return ChatService.USER_TYPING + "." + room
  }

  private roomCreated = new Subject<Room>();
  private roomDeleted = new Subject<string>();
  private roomSelected = new Subject<Room>();
  private roomJoined = new Subject<Participation>();
  private roomLeft = new Subject<Participation>();
  private messageSent = new Subject<Message>();

  constructor(
    private http: HttpClient,
    private eventBus: EventBusService,
    private auth: AuthService
  ) {
    eventBus.connect(ChatService.EVENTS);

    eventBus.registerHandler(ChatService.ROOM_DELETED, (err, msg) => {
      this.roomDeleted.next(msg.body.name);
    });

    eventBus.registerHandler(ChatService.ROOM_JOINED, (err, msg) => {
      console.log("room joined");
      console.log(msg.body);
      this.roomJoined.next(msg.body);
    });

    eventBus.registerHandler(ChatService.ROOM_LEFT, (err, msg) => {
      this.roomLeft.next(msg.body);
    });

    eventBus.registerHandler(ChatService.MESSAGE_SENT, (err, msg) => {
      console.log(msg.body);
      this.messageSent.next(msg.body);
    });

    eventBus.registerHandler(ChatService.ROOM_CREATED, (err, msg) => {
      this.roomCreated.next(msg.body);
    });
  }

  getRooms(): Observable<Room[]> {
    const user = this.auth.user;
    return this.http.get<Room[]>(ChatService.ROOMS, {
      headers: this.auth.authOptions,
      params: {
        user: user.username
      }
    });
  }

  getUserParticipations(): Observable<Room[]> {
    const user = this.auth.user;
    return this.http.get<Room[]>(`${ChatService.USERS}/${user.username}/participations`, {
      headers: this.auth.authOptions
    });
  }

  getMessagesOnRoom(name: string): Observable<Message[]> {
    return this.http.get<Message[]>(ChatService.ROOMS + '/' + name + '/messages', {
      headers: this.auth.authOptions
    });
  }

  selectRoom(room: Room) {
    this.roomSelected.next(room);
  }

  onRoomSelected(): Observable<Room> {
    return this.roomSelected.asObservable();
  }

  createRoom(name: string): Observable<string> {
    const user = this.auth.user;
    return this.http
      .post<Room>(ChatService.ROOMS, new CreateRoomRequest(name, user.username), {
        headers: this.auth.authOptions
      })
      .pipe(map(room => room.name));
  }

  deleteRoom(name: string): Observable<void> {
    const user = this.auth.user;
    const body = new DeleteRoomRequest(name, user.username);
    return this.http.request<void>('delete', ChatService.ROOMS + '/' + name, {
      body: body,
      headers: this.auth.authOptions
    });
  }

  joinRoom(name: string): Observable<Participation> {
    const user = this.auth.user;
    const body = new JoinRoomRequest(user.username);
    return this.http.post<Participation>(ChatService.ROOMS + '/' + name + '/participations', body, {
      headers: this.auth.authOptions
    });
  }

  getRoomParticipations(name: string): Observable<Participation[]> {
    return this.http.get<Participation[]>(
      ChatService.ROOMS + '/' + name + '/participations?user=' + this.auth.user.username, {
        headers: this.auth.authOptions
      });
  }

  leaveRoom(name: string): Observable<void> {
    const user = this.auth.user;
    return this.http.delete<void>(
      ChatService.ROOMS + '/' + name + '/participations/' + user.username, {
        headers: this.auth.authOptions
    });
  }

  sendMessage(name: string, message: string): Observable<void> {
    const user = this.auth.user;
    const body = new SendMessageRequest(user.username, message);
    return this.http.post<void>(ChatService.ROOMS + '/' + name + '/messages',
      body, {
      headers: this.auth.authOptions
    });
  }

  notifyTyping(username: string, room: string) {
    const writingUser = new NotifyWritingUserRequest(username, room);
    this.eventBus.publish(ChatService.USER_TYPING, writingUser)
  }

  registerUsersTypingListener(room: string, writingUsers: string[]){
    this.eventBus.registerHandler(ChatService.userTypingAddress(room), (err, msg) => {
      console.log(msg.body);
      if(msg.body !== this.auth.user.username){
        writingUsers.push(msg.body);
        timer(1500).subscribe(() => writingUsers.shift(), err => console.log(err));
      }
    });
  }

  unregisterUsersTypingListener(room: string){
    this.eventBus.unregister(ChatService.userTypingAddress(room))
  }

  onRoomCreated(): Observable<Room> {
    return this.roomCreated.asObservable();
  }

  onRoomDeleted(): Observable<string> {
    return this.roomDeleted.asObservable();
  }

  onRoomJoined(): Observable<Participation> {
    return this.roomJoined.asObservable();
  }

  onRoomLeft(): Observable<Participation> {
    return this.roomLeft.asObservable();
  }

  onMessageSent(): Observable<Message> {
    return this.messageSent.asObservable();
  }

}
