import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {RegisterRequest} from '../requests';
import {AuthService} from '../service/auth.service';
import {Toast} from "../toast-notify";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {

  request = new RegisterRequest();

  constructor(private router: Router, private auth: AuthService) {}

  ngOnInit() {}

  register() {
    if (!new RegExp(/^[A-Za-z0-9]+$/).test(this.request.username)){
      Toast.toast("Username not permitted. You can't use spaces or special characters");
    } else {
      this.auth.register(this.request).subscribe(
        user => {
        },
        err => {
          const error = err.error.error;
          Toast.toast("Registration failed! \n" + error.type + " error. \n Make sure to fill correctly all fields.");
          console.error(err);
          this.request.password = '';
          this.request.passwordConfirm = '';
        },
        () => this.router.navigateByUrl('/')
      );
    }
  }

}
