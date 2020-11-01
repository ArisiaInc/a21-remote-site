import { Component, OnInit, Input } from '@angular/core';
import { ProgramPerson } from '@app/_models';
import { ActivatedRoute } from '@angular/router';
import { ScheduleService } from '../_services/schedule.service';
import { pluck, flatMap } from 'rxjs/operators'

@Component({
  selector: 'app-person',
  templateUrl: './person.component.html',
  styleUrls: ['./person.component.scss']
})
export class PersonComponent implements OnInit {
  person: ProgramPerson;

  constructor(private route: ActivatedRoute,
    private scheduleService: ScheduleService) { }

  ngOnInit(): void {
    this.route.params.pipe(
      pluck('id'),
      flatMap(id => this.scheduleService.get_person(id))
    ).subscribe(person => this.person = person);
  }

}
