import { Component, OnInit } from '@angular/core';

import { ScheduleService } from '@app/_services';

@Component({
  selector: 'app-people',
  templateUrl: './people.component.html',
  styleUrls: ['./people.component.scss']
})
export class PeopleComponent implements OnInit {
  // people$ are scheduleService.display$ now

  constructor(
    public scheduleService: ScheduleService) {
  }

  ngOnInit(): void {
  }
}
