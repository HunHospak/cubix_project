import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth.service';
import { LoginService } from '../login.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  username: string;
  password: string;

  constructor(private loginService: LoginService, private authService: AuthService) { }

  ngOnInit(): void {
  }

  onLogin(){
    this.loginService.login(this.username, this.password)
      .subscribe(token => this.authService.saveToken(token));
  }

  onLogout(){
    this.authService.removeToken();
  }

  isLoggedIn(): boolean{
    return this.authService.getToken() != null;
  }
}
