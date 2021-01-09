import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { ScheduleService, Room } from '@app/_services';

@Component({
  selector: 'app-programming',
  templateUrl: './programming.component.html',
  styleUrls: ['./programming.component.scss']
})
export class ProgrammingComponent implements OnInit {
  rooms$!: Observable<Room[]>;

  constructor(private scheduleService: ScheduleService) { }

  ngOnInit(): void {
    this.rooms$ = this.scheduleService.getRooms();
  }

}
