import { Component, OnInit } from '@angular/core';
import { ProgramPerson } from '@app/_models';
import { ScheduleService } from '@app/_services';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-people',
  templateUrl: './people.component.html',
  styleUrls: ['./people.component.scss']
})
export class PeopleComponent implements OnInit {
  people$!: Observable<ProgramPerson[]>;

  constructor(
    public scheduleService: ScheduleService
  ) { }

  ngOnInit(): void {}

}
