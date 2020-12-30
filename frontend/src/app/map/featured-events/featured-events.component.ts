import { Component, OnInit, HostBinding } from '@angular/core';
import { Observable } from 'rxjs';

import { ScheduleService, StructuredEvents } from '@app/_services';

@Component({
  selector: 'app-featured-events',
  templateUrl: './featured-events.component.html',
  styleUrls: ['./featured-events.component.scss']
})
export class FeaturedEventsComponent implements OnInit {
  events$!: Observable<StructuredEvents>;

  @HostBinding('class') class = 'm-2';

  constructor( private scheduleService: ScheduleService) { }

  ngOnInit(): void {
    this.events$ = this.scheduleService.get_featured_events()
  }

}
