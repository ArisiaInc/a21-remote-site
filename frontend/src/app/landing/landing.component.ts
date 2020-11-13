import { Component, OnInit } from '@angular/core';
import { AccountService } from '@app/_services';
import { Router } from '@angular/router';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent implements OnInit {

  constructor(
    private accountService: AccountService,
    private router: Router
  ) { }

  ngOnInit(): void {
  }

  logout() {
    this.accountService.userValue = false;
    console.log('logged out');
    this.router.navigate(['/account/login']);
  }

}
