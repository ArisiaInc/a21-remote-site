import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AccountService } from '@app/_services';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private router: Router,
    private accountService: AccountService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
      return this.accountService.loggedIn$.pipe(
        map(logged_in => {
          if (logged_in) {
            return true;
          }
          this.router.navigate(['/account/login'], {queryParams: {returnUrl: state.url}});
          return false;

        }),
        catchError( err => {
          this.router.navigate(['/account/login'], {queryParams: {returnUrl: state.url}});
          return of(false);
        })
      );
  }

}
