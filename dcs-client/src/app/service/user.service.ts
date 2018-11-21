import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AuthService} from './auth.service';
import {Observable, Subject} from 'rxjs';
import {User} from '../model';
import {EditProfileRequest} from '../requests';
import {EventBusService} from './event-bus.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private static API_PREFIX = '/api';

  private static EVENTS = UserService.API_PREFIX + '/events';

  private static USERS = UserService.API_PREFIX + '/users';

  private static USER_ONLINE = 'users.online';
  private static USER_OFFLINE = 'users.offline';

  private static USER_HEARTBEAT_REQUEST = 'users.heartbeat.request';
  private static USER_HEARTBEAT_RESPONSE = 'users.heartbeat.response';

  private userOnline = new Subject<User>();
  private userOffline = new Subject<User>();

  constructor(
    private http: HttpClient,
    private eventBus: EventBusService,
    private auth: AuthService
  ) {
    eventBus.connect(UserService.EVENTS);

    eventBus.registerHandler(UserService.USER_ONLINE, (err, msg) => {
      this.userOnline.next(msg.body);
    });

    eventBus.registerHandler(UserService.USER_OFFLINE, (err, msg) => {
      this.userOffline.next(msg.body);
    });

    eventBus.registerHandler(
      UserService.USER_HEARTBEAT_REQUEST,
      (err, msg) => {
        if (auth.isAuthenticated) {
          const username = auth.user.username;
          eventBus.send(UserService.USER_HEARTBEAT_RESPONSE, {
            username
          });
        }
      }
    );
  }

  editProfile(request: EditProfileRequest): Observable<User> {
    return this.http.put<User>(
      UserService.USERS + '/' + request.username,
      request,
      {
        headers: this.auth.authOptions
      }
    );
  }

  getProfile(username: string): Observable<User> {
    return this.http.get<User>(UserService.USERS + '/' + username, {
      headers: this.auth.authOptions
    });
  }

  onUserOnline(): Observable<User> {
    return this.userOnline.asObservable();
  }

  onUserOffline(): Observable<User> {
    return this.userOffline.asObservable();
  }

}
