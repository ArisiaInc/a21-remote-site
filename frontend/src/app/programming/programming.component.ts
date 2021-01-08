import { Component, OnInit } from '@angular/core';

import { ScheduleService } from '@app/_services';

@Component({
  selector: 'app-programming',
  templateUrl: './programming.component.html',
  styleUrls: ['./programming.component.scss']
})
export class ProgrammingComponent implements OnInit {

  constructor(public scheduleService: ScheduleService) { }

  ngOnInit(): void {
  }

}
