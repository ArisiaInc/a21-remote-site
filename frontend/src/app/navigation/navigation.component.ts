import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { Component, OnDestroy, HostBinding } from '@angular/core';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

import { AccountService } from '@app/_services';


@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss']
})
export class NavigationComponent implements OnDestroy {
  @HostBinding('class') class = 'sticky-top';
  currentRoute: string = '';
  subscription: Subscription;

  public isMenuCollapsed = true;

  constructor(public route: ActivatedRoute,
              private router: Router,
              public accountService: AccountService) {

    this.subscription =
      router.events.pipe(
        filter(event => event instanceof NavigationEnd),
      ).subscribe(event => this.currentRoute = (event as NavigationEnd).url);
  }

  ngOnDestroy() {
    this.subscription.unsubscribe;
  }
}
