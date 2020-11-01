import { Injectable } from '@angular/core';
import { Observable, of, zip } from 'rxjs';
import { map, groupBy, mergeMap, toArray, filter, tap } from 'rxjs/operators';
import { ProgramItem, ProgramPerson, ProgramFilter } from '@app/_models';

import { program, people } from "test_data/konopas"

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {

  constructor() { }

  get_schedule(filters: ProgramFilter = {}): Observable<[string, string, ProgramItem[]]> {
    const munged_filters: { tags?: (_:string[]) => boolean, loc?: (_:string[]) => boolean, date?: (_:string) => boolean } = {}
    Object.keys(filters).forEach(f => {
        munged_filters[f] = pf => filters[f].some(f => pf.includes(f) || pf === f)
    });
    return of(...program).pipe(
      filter(p => Object.keys(munged_filters).every(k => munged_filters[k](p[k]))),
      groupBy(p => `${p.date}~${p.time}`),
      mergeMap(group => zip(of(group.key.split('~')[0]), of(group.key.split('~')[1]), group.pipe(toArray())))
    );
  }

  get_people(): Observable<ProgramPerson[]> {
    return of(people);
  }

  get_person(id): Observable<ProgramPerson> {
    return this.get_people().pipe(map(people => people.find(p => p.id === id)));
  }
}
