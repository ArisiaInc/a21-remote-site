import { Injectable } from '@angular/core';
import { ReplaySubject, Observable } from 'rxjs';

export interface Initial {
  lower: string;
  upper: string;
  active: boolean;
}

@Injectable({
  providedIn: 'root'
})
export abstract class XByNameService<T> {

  constructor() { }

  all$ = new ReplaySubject<T[]>(1);
  display$ = new Observable<T[]>();
  initials$ =  new ReplaySubject<{[lower: string]: Initial}>(1);

  abstract search(search: string) : Observable<T[]>;
}
