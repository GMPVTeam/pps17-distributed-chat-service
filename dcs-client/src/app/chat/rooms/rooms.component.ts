import {Component, OnInit} from '@angular/core';
import {ChatService} from '../../service/chat.service';
import {Router} from '@angular/router';
import {remove} from 'lodash';

import {Participation, Room} from '../../model';
import {AuthService} from '../../service/auth.service';

import {filter} from 'rxjs/operators';

@Component({
  selector: 'app-rooms',
  templateUrl: './rooms.component.html',
  styleUrls: ['./rooms.component.scss']
})
export class RoomsComponent implements OnInit {

  rooms: Room[] = [];

  constructor(
    private chat: ChatService,
    private auth: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    console.log('Getting user rooms...');
    const participationFilter = filter<Participation>(p => p.username === this.auth.user.username);

    this.chat.onRoomLeft()
      .pipe(participationFilter)
      .subscribe(participation => {
        this.removeRoom(participation.room.name);
      });

    this.chat.onRoomJoined()
      .pipe(participationFilter)
      .subscribe(participation => {
        console.log("on room joined called! Room: " + participation.room.name);
        this.rooms.push(participation.room)
      }
    );

    this.chat.getUserParticipations()
      .subscribe(rooms => this.rooms = rooms);

    this.chat.onRoomDeleted()
      .subscribe(name => this.removeRoom(name));
  }

  selectRoom(room: Room) {
    this.chat.selectRoom(room);
    this.router.navigate(['/rooms', room.name]);
  }

  deleteRoom(room: Room) {
    console.log(room);
    this.chat.deleteRoom(room.name)
      .subscribe(() => this.router.navigate(['/']), err => console.error(err));
  }

  private removeRoom(name: String) {
    remove(this.rooms, room => room.name === name);
  }

}
