import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';
import { Observable, of, pipe, OperatorFunction } from 'rxjs';
import { Creator } from '@app/_models';
import { flatMap, filter, toArray,} from 'rxjs/operators';
import { XByNameService } from './x-by-name.service';

@Injectable({
  providedIn: 'root'
})
export abstract class CreatorService extends XByNameService<Creator>{
  http: HttpClient;

  constructor(http: HttpClient) {
    super();
    this.http = http;
   }

  abstract get_data(local:boolean): Observable<Creator[]>;

  make_id_filter(ids?: string[]) : OperatorFunction<Creator[], Creator[]> {
    if (ids) {
      return pipe(
        flatMap(x => of(...x)),
        filter((c: Creator) => ids.includes(c.id)),
        toArray()
      )
    }
    return pipe()
  }

  abstract goh_filter(): OperatorFunction<Creator[], Creator[]>;

  search(search: string) : Observable<Creator[]> {
    if (search === 'goh') {
      return this.get_creators().pipe(
        this.goh_filter()
      )
    }
    return this.get_creators().pipe(
      this.search_alpha(search)
    )
  }

  get_creators(ids?: string[]) : Observable<Creator[]>{
    return this.get_data(environment.local_data).pipe(
      this.make_initials(),
      this.make_id_filter(ids),
    );
  }
}
