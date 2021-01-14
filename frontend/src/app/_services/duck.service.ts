import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';
import { Observable, ReplaySubject, of } from 'rxjs';
import { Duck, DuckState } from '@app/_models';
import { tap, catchError, mapTo, map, filter } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})

export class DuckService {

  ducks: Duck[] = [];
  ducks$ = new ReplaySubject<Duck[]>(1);

  constructor(private http: HttpClient) {
    this.load();
  }

  load(): void {
    this.http.get<Duck[]>(`${environment.backend}/ducks`).subscribe(response => this.handleResponse(response));
  }

  handleResponse(response: Duck[]): void {
    this.ducks = response;
    this.ducks$.next(this.ducks);
  }

  getDuckStates(ids: Set<number>, config: {includeHidden: boolean}) : Observable<DuckState[]> {
    return this.ducks$.pipe(
      map(ducks => {
        const unfiltered = ducks.map(duck => ({duck, hidden: !ids.has(duck.id)}));
        if (config.includeHidden) {
          return unfiltered;
        } else {
          return unfiltered.filter(({hidden}) => !hidden);
        }
      })
    );
  }

  get_duck(id: number) : Observable<Duck | undefined> {
    return this.http.get<Duck|undefined>(`${environment.backend}/ducks/${id}`);
  }

  award_duck(id: number) {
    return this.http.post<any>(`${environment.backend}/ducks/${id}`, {}, {withCredentials: true}).pipe(
      catchError(e => of(false))
    );
  }
}
