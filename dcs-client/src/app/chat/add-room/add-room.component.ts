import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ChatService} from '../../service/chat.service';
import {Toast} from "../../toast-notify";

@Component({
  selector: 'app-add-room',
  templateUrl: './add-room.component.html',
  styleUrls: ['./add-room.component.scss']
})
export class AddRoomComponent implements OnInit {

  name = '';

  constructor(private router: Router, private chat: ChatService) {
  }

  ngOnInit() {
    this.chat.selectRoom({
      name: ''
    });
  }

  addRoom() {
    if (!new RegExp(/^[A-Za-z0-9]+$/).test(this.name)){
      Toast.toast("Name not permitted. You can't use spaces or special characters");
    } else {
      this.chat.createRoom(this.name)
        .subscribe(
          name => {
            this.chat.selectRoom({name: name});
            this.router.navigate(['/rooms', name]);
          },
          err => console.error(err)
        );
    }
  }

}
