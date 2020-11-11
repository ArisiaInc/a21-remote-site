import { Component, OnInit } from '@angular/core';
import { AccountService } from '@app/_services';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  returnUrl: string;

  constructor(
    private accountService: AccountService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  login() {
    this.accountService.login('joe', 'volcano').subscribe( retval => {
      console.log(retval);
      this.accountService.userValue = true;
      console.log('logged in');
      this.router.navigate([this.returnUrl]);
    })
  }

}
