import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ScheduleService, SchedulePerson } from '@app/_services/schedule.service';
import { pluck, flatMap, map } from 'rxjs/operators'
import { Observable } from 'rxjs';

@Component({
  selector: 'app-person',
  templateUrl: './person.component.html',
  styleUrls: ['./person.component.scss']
})
export class PersonComponent implements OnInit {
  @Input() person!: SchedulePerson;
  expanded = false;

  constructor() { }

  ngOnInit(): void {
  }

}
