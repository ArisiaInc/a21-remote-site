import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { pluck, switchMap } from 'rxjs/operators';

import { User } from '@app/_models';
import { AccountService } from '@app/_services';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit {
  user$!: Observable<User|undefined>;

  constructor(private route : ActivatedRoute,
    private accountService: AccountService) { }

  ngOnInit(): void {
    this.user$ = this.route.params.pipe(
      pluck('id'),
      switchMap(id => this.accountService.getUser(id)),
    );
  }
}
