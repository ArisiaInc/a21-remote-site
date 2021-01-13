import { Component, OnInit } from '@angular/core';
import { ActivatedRoute} from '@angular/router';
import { Observable, of } from 'rxjs';
import { pluck, switchMap } from 'rxjs/operators';

import { ScheduleService, SchedulePerson } from '@app/_services';

@Component({
  selector: 'app-person-page',
  templateUrl: './person-page.component.html',
  styleUrls: ['./person-page.component.scss']
})
export class PersonPageComponent implements OnInit {
  person$!: Observable<SchedulePerson | undefined>;

  constructor(
    private route: ActivatedRoute,
    public scheduleService: ScheduleService) {
  }

  ngOnInit(): void {
    this.person$ = this.route.params.pipe(
      pluck('id'),
      switchMap(id => {
        if (id) {
          return this.scheduleService.getPerson(id);
        } else {
          return of(undefined);
          // Redirect to 404 page.
        }
      }),
    );
  }

}
