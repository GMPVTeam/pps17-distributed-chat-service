import {Component, OnInit} from '@angular/core';
import {NavigationEnd, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {ChatService} from '../../service/chat.service';
import {AuthService} from '../../service/auth.service';

@Component({
  selector: 'app-topnav',
  templateUrl: './topnav.component.html',
  styleUrls: ['./topnav.component.scss']
})
export class TopnavComponent implements OnInit {

  pushRightClass = 'push-right';

  selectedRoom: string;

  roomInfoOpened: boolean;

  constructor(
    private router: Router,
    private auth: AuthService,
    private chat: ChatService,
    private translate: TranslateService
  ) {
    this.roomInfoOpened = false;
  }

  ngOnInit() {
    this.router.events.subscribe(val => {
      if (
        val instanceof NavigationEnd &&
        window.innerWidth <= 992 &&
        this.isToggled()
      ) {
        this.toggleSidebar();
      }
    });

    this.chat.onRoomSelected()
      .subscribe(room => {
        this.selectedRoom = room.name;
        this.roomInfoOpened = false;
      });

    this.chat.onRoomDeleted().subscribe(name => {
      if (name === this.selectedRoom) {
        this.resetSelectedRoomName();
        this.router.navigateByUrl('/')
      }
    });
  }

  private resetSelectedRoomName() {
    this.selectedRoom = '';
  }

  isToggled(): boolean {
    const dom: Element = document.querySelector('body');
    return dom.classList.contains(this.pushRightClass);
  }

  toggleSidebar() {
    const dom: Element = document.querySelector('body');
    dom.classList.toggle(this.pushRightClass);
  }

  logout() {
    this.auth.logout().subscribe(
      _ => {
      },
      err => console.error(err),
      () => this.router.navigateByUrl('/login'));
  }

  editProfile() {
    this.router.navigate(['/users', this.auth.user.username, 'edit']);
  }

  changeLang(language: string) {
    this.translate.use(language);
  }

  createRoom() {
    this.router.navigateByUrl('/add-room');
  }

  roomInfo() {
    if (!this.roomInfoOpened) {
      this.router.navigate(['/rooms', this.selectedRoom, 'info']);
      this.roomInfoOpened = true;
    } else {
      this.router.navigate(['/rooms', this.selectedRoom]);
      this.roomInfoOpened = false;
    }
  }

  get infoButtonCondition(): boolean {
    return this.selectedRoom != undefined && this.selectedRoom != '' &&
      !this.roomInfoOpened && !this.inProfileEditPage()
  }

  goBack(): void {
    if (this.selectedRoom == undefined) {
      this.router.navigate(['/']);
    } else {
      this.router.navigate(['/rooms', this.selectedRoom]);
    }
  }

  leaveRoom() {
    console.log('Leaving room: ' + this.selectedRoom);
    this.chat.leaveRoom(this.selectedRoom)
      .subscribe(() => {
        this.router.navigate(['/']).then(succeded => {
          if (succeded) {
            this.resetSelectedRoomName();
          }
        })
      }, err => console.error(err));
  }

  inProfileEditPage(): boolean {
    return this.router.url.match('.*\\/users\\/.*\\/edit') != null
  }

  inRoomSelectedPage(): boolean {
    return this.router.url.match('.*\\/rooms\\/.*') != null
  }

  leaveRoomButtonVisible(): boolean {
    return this.selectedRoom != undefined && this.selectedRoom != ''
      && (!this.inProfileEditPage()) && !this.roomInfoOpened
  }
}
