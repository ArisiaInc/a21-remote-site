import { Component, OnInit } from '@angular/core';
import { ActivatedRoute} from '@angular/router';
import { Observable, of } from 'rxjs';
import { pluck, switchMap } from 'rxjs/operators';

import { ScheduleService, ScheduleEvent } from '@app/_services';

@Component({
  selector: 'app-event-page',
  templateUrl: './event-page.component.html',
  styleUrls: ['./event-page.component.scss']
})
export class EventPageComponent implements OnInit {
  event$!: Observable<ScheduleEvent | undefined>;
  openDoors$!: Observable<Set<string>>;

  constructor(
    private route: ActivatedRoute,
    public scheduleService: ScheduleService) {
  }

  ngOnInit(): void {
    this.event$ = this.route.params.pipe(
      pluck('id'),
      switchMap(id => {
        if (id) {
          return this.scheduleService.getEvent(id);
        } else {
          return of(undefined);
          // Redirect to 404 page.
        }
      }),
    );
  }

}
