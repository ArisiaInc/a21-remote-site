import { Injectable } from '@angular/core';
import { Observable, of, zip, OperatorFunction } from 'rxjs';
import { map, groupBy, mergeMap, toArray, filter, tap, flatMap, pluck, every } from 'rxjs/operators';
import { ProgramItem, ProgramPerson, ProgramFilter } from '@app/_models';

import { program, people } from "test_data/konopas"
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';

interface ScheduleData {
  program: ProgramItem[],
  people: ProgramPerson[]
}

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {

  constructor(private http: HttpClient) { }

  get_data() : Observable<ScheduleData>{

    // real is currently broken, we'll start with fake
    return of<ScheduleData>({program, people});
    // this is real
    // return this.http.get<ScheduleData>(`${environment.backend}/schedule`);
  }

  get_schedule(filters: ProgramFilter = {}): Observable<[string, string, ProgramItem[]]> {
    const munged_filters: { tags?: (_:string[]) => boolean, loc?: (_:string[]) => boolean, date?: (_:string) => boolean } = {}
    Object.keys(filters).forEach(f => {
        munged_filters[f] = pf => filters[f].some(f => pf.includes(f) || pf === f)
    });
    return this.get_data().pipe(
      pluck('program'),
      flatMap(x => of(...x)),
      filter(p => Object.keys(munged_filters).every(k => munged_filters[k](p[k]))),
      groupBy(p => `${p.date}~${p.time}`),
      mergeMap(group => zip(of(group.key.split('~')[0]), of(group.key.split('~')[1]), group.pipe(toArray()))),
    );
  }

  get_people(): Observable<ProgramPerson[]> {
    return this.get_data().pipe(
      pluck('people'),
    );
  }

  get_person(id): Observable<ProgramPerson> {
    return this.get_people().pipe(
      map(people => people.find(p => p.id === id)),
    );
  }

  get_featured_events(): Observable<ProgramItem[]> {
    // for testing:
    return of(['13','16','29']).pipe(
    //return this.http.get<String[]>(`${environment.backend}/schedule/featured`).pipe(
      flatMap(ids => this.get_data().pipe(
        pluck('program'),
        flatMap(x => of(...x)),
        filter(p => ids.includes(p.id)),
        toArray()
      )),
    );
  }
}
