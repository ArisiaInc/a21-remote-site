import { Component, OnInit } from '@angular/core';
import { ActivatedRoute} from '@angular/router';
import { Observable } from 'rxjs';
import { pluck, switchMap, map } from 'rxjs/operators';

import { ProgramPerson } from '@app/_models';
import { ScheduleService } from '@app/_services';

@Component({
  selector: 'app-people',
  templateUrl: './people.component.html',
  styleUrls: ['./people.component.scss']
})
export class PeopleComponent implements OnInit {
  people$!: Observable<ProgramPerson[]>;
  search$!: Observable<string>;
  links$!: Observable<{ link: string,
                        searchCompare: string,
                        display: string,
                        active: boolean,
         }[]>

  constructor(
    private route: ActivatedRoute,
    private scheduleService: ScheduleService) {
    this.links$ = this.scheduleService.peopleInitials$.pipe(
      map(initials =>
        [{link: '', searchCompare: ' ', display: 'All', active: true},
         {link: 'goh', searchCompare: 'goh', display: 'GOH', active: true}].
        concat(Object.values(initials).map(({lower, upper, active}) => ({link: lower, searchCompare: lower, display: upper, active})))
         ),
    );
  }

  ngOnInit(): void {
    this.search$ = this.route.params.pipe(
      pluck('search'),
      map(search => search && search.toLowerCase()),
    );
    this.people$ = this.search$.pipe(
      switchMap(search => {
        if (search) {
          return this.scheduleService.getPeople(search);
        } else {
          return this.scheduleService.people$;
        }
      }),
    );
  }

}
