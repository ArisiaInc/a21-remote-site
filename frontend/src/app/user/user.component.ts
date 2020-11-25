import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { User } from '@app/_models';
import { AccountService } from '@app/_services';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit {
  id: string;
  subscription: Subscription;
  user: User;
  badgeNum: number;

  constructor(private route : ActivatedRoute,
    private accountService: AccountService) { }

  ngOnInit(): void {
    this.subscription = this.route.params.subscribe( params => {
      this.id = params.id;
      this.accountService.getUser(this.id).subscribe( user => this.user = user );
    })
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  giveBadge() {
    this.accountService.giveBadge(this.id, this.badgeNum).subscribe( user => this.user = user);
  }

}
