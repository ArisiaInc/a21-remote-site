import { Component, OnInit } from '@angular/core';
import { AccountService } from '@app/_services';
import { Router } from '@angular/router';
import { User } from '@app/_models';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent implements OnInit {
  public user?: User;

  constructor(
    private accountService: AccountService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.user = this.accountService.user
  }

  logout() {
    this.accountService.logout().subscribe();
    console.log('logged out');
    this.router.navigate(['/account/login']);
  }

}
