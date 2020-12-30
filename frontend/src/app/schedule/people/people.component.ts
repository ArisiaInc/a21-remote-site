import { Component, OnInit } from '@angular/core';
import { ProgramPerson } from '@app/_models';
import { ScheduleService } from '@app/_services/schedule.service';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

function personName(person: ProgramPerson): string {
  return Array.isArray(person.name) ? person.name.join(' ') : person.name;
}

@Component({
  selector: 'app-people',
  templateUrl: './people.component.html',
  styleUrls: ['./people.component.scss']
})
export class PeopleComponent implements OnInit {
  people$!: Observable<ProgramPerson[]>;

  constructor(
    private scheduleService: ScheduleService
  ) { }

  ngOnInit(): void {
    this.people$ = this.scheduleService.get_people().pipe(
      map(people => [...people].sort(
        (a, b) => (
          personName(a).localeCompare(personName(b)))))
    );
  }

}
