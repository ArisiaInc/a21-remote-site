import { Component, OnChanges, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ScheduleService, SchedulePerson } from '@app/_services/schedule.service';
import { pluck, flatMap, map } from 'rxjs/operators'
import { Observable } from 'rxjs';

@Component({
  selector: 'app-person',
  templateUrl: './person.component.html',
  styleUrls: ['./person.component.scss']
})
export class PersonComponent implements OnChanges {
  @Input() person!: SchedulePerson;
  @Input() alwaysExpanded = false;
  expanded = false;

  constructor() { }

  ngOnChanges(): void {
    if (this.alwaysExpanded) {
      this.expanded = true;
    }
  }

}
