import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { ScheduleService, StructuredEvents } from '@app/_services';
import { ProgramFilter } from '@app/_models';

@Component({
  selector: 'app-starred',
  templateUrl: './starred.component.html',
  styleUrls: ['./starred.component.scss']
})
export class StarredComponent implements OnInit {
  events$!: Observable<StructuredEvents>;

  constructor(private scheduleService: ScheduleService) { }

  ngOnInit(): void {
    this.events$ = this.scheduleService.getStarredEvents();
  }

  updateItems(filters: ProgramFilter) {
    this.events$ = this.scheduleService.getStarredEvents(filters);
  }
}
