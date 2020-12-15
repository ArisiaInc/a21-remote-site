import { Component, OnInit } from '@angular/core';
import { ProgramPerson } from '@app/_models';
import { ScheduleService } from '@app/_services/schedule.service';

@Component({
  selector: 'app-people',
  templateUrl: './people.component.html',
  styleUrls: ['./people.component.scss']
})
export class PeopleComponent implements OnInit {
  people: ProgramPerson[] = [];

  constructor(
    private scheduleService: ScheduleService
  ) { }

  ngOnInit(): void {
    this.scheduleService.get_people().subscribe(people => this.people = people.sort((a, b)=> (Array.isArray(a.name) ? a.name.join(' ') : a.name).localeCompare(Array.isArray(b.name) ? b.name.join(' ') : b.name)))
  }

}
