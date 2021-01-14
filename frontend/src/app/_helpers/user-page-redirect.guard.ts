import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { AccountService } from '@app/_services';

@Injectable({
  providedIn: 'root'
})
export class UserPageRedirectGuard implements CanActivate {
  constructor(private router: Router, private accountService: AccountService) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.accountService.me$.pipe(
      map(me => me ? this.router.parseUrl(`/me/${me.badgeNumber}`) : false),
    );
  }
}
