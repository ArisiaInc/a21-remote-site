import { Injectable } from '@angular/core';
import { ReplaySubject, Observable, pipe, OperatorFunction } from 'rxjs';
import { tap, map } from 'rxjs/operators';

export interface Initial {
  lower: string;
  upper: string;
  active: boolean;
}

export interface Named {
  name: string;
  id: string;
}

@Injectable({
  providedIn: 'root'
})
export abstract class XByNameService<T extends Named> {
  constructor() { }

  all$ = new ReplaySubject<T[]>(1);
  display$ = new Observable<T[]>();
  initials$ =  new ReplaySubject<{[lower: string]: Initial}>(1);
  
  protected initials: {[lower: string]: Initial} = {};

  make_initials() : OperatorFunction<T[], T[]> {
    for(let letter = 1; letter <= 26; letter++) {
      const lower = String.fromCharCode(letter + 96);
      const upper = String.fromCharCode(letter + 64);
      this.initials[lower] = {lower, upper, active: false};
    }

    return pipe(
      tap(ts => {
        ts.forEach(t => {
          const lower = t.name[0].toLowerCase();
          if (this.initials[lower]) {
            this.initials[lower].active = true;
          } else {
            const upper = t.name[0].toUpperCase();
            this.initials[lower] = {lower, upper, active: true};
          }
        });
        this.initials$.next(this.initials);
      })
    );
  }

  search_alpha(search: string) : OperatorFunction<T[], T[]> {
    const regexp = new RegExp('^'+search, 'i');
    return pipe(
      map(ts => ts.filter(t => t.name.match(regexp)))
    )
  }

  abstract search(search: string) : Observable<T[]>;
}
