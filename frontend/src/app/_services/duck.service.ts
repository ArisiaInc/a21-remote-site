import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';
import { Observable, of } from 'rxjs';
import { Duck, DuckState } from '@app/_models';
import { catchError, map } from 'rxjs/operators';

import { MetadataCacher } from './metadata-cacher';

@Injectable({
  providedIn: 'root'
})

export class DuckService {
  private metadataCacher: MetadataCacher<Duck[]>;
  ducks$: Observable<Duck[]>;

  constructor(private http: HttpClient) {
    this.metadataCacher = new MetadataCacher<Duck[]>(http, `${environment.backend}/ducks`, []);
    this.ducks$ = this.metadataCacher.data$;
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

  getDuck(id: number) : Observable<Duck | undefined> {
    return this.ducks$.pipe(
      map(ducks => ducks.find(({id: duckId}) => id === duckId)),
    );
  }
}
