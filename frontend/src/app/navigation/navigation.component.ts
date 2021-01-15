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
  selectedKey: string;

  public isMenuCollapsed = true;

  constructor(public route: ActivatedRoute,
              private router: Router,
              public accountService: AccountService) {
    this.selectedKey = '/map';

    this.subscription =
      router.events.pipe(
        filter(event => event instanceof NavigationEnd),
      ).subscribe(event => {
        this.currentRoute = (event as NavigationEnd).url;
        if (this.currentRoute.startsWith('/schedule/program')) {
          this.selectedKey = '/schedule/program';
        } else if (this.currentRoute.startsWith('/schedule/people')) {
          this.selectedKey = '/schedule/people';
        } else if (this.currentRoute.startsWith('/schedule/starred')) {
          this.selectedKey = '/schedule/starred';
        } else if (this.currentRoute.startsWith('/schedule')) {
          this.selectedKey = '/schedule';
        } else if (this.currentRoute.startsWith('/user')) {
          this.selectedKey = '/user';
        } else if (this.currentRoute.startsWith('/account/login')) {
          this.selectedKey = '/account/login';
        } else if (this.currentRoute.startsWith('/help')) {
          this.selectedKey = '/help';
        } else {
          this.selectedKey = '/map';
        }
      });

  }

  ngOnDestroy() {
    this.subscription.unsubscribe;
  }

  onLogout() {
    this.accountService.logout().subscribe( _ => {
      this.router.navigateByUrl('/account/login?loggedOut=1');
      this.isMenuCollapsed = true;
    }, error => {
      // TODO show something to the user here about 401s?
      console.error(error);
    });
  }
}
