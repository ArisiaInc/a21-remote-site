import { Component, OnInit } from '@angular/core';
import { ActivatedRoute} from '@angular/router';
import { Observable } from 'rxjs';
import { pluck, switchMap, map } from 'rxjs/operators';

import { ScheduleService, SchedulePerson } from '@app/_services';

@Component({
  selector: 'app-people',
  templateUrl: './people.component.html',
  styleUrls: ['./people.component.scss']
})
export class PeopleComponent implements OnInit {
  people$!: Observable<SchedulePerson[]>;
  search$!: Observable<string>;

  constructor(
    private route: ActivatedRoute,
    public scheduleService: ScheduleService) {
  }

  ngOnInit(): void {
    this.search$ = this.route.params.pipe(
      pluck('search'),
      map(search => (search || '').toLowerCase()),
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
