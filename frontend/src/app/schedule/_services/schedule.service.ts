import { Injectable } from '@angular/core';
import { Observable, of, GroupedObservable, zip } from 'rxjs';
import { map, groupBy, mergeMap, toArray } from 'rxjs/operators';
import { ProgramItem, ProgramPerson } from '@app/_models';

import { program, people } from "test_data/konopas"

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {

  constructor() { }

  get_schedule(): Observable<[string, string, ProgramItem[]]> {
    return of(...program).pipe(
      groupBy(p => `${p.date}~${p.time}`),
      mergeMap(group => zip(of(group.key.split('~')[0]), of(group.key.split('~')[1]), group.pipe(toArray())))
    );
  }

  get_people(): Observable<ProgramPerson[]> {
    return of(people);
  }

  get_person(id): Observable<ProgramPerson> {
    return this.get_people().pipe(map(people => people.filter(p => p.id === id)[0]));
  }
}
