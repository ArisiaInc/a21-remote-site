import { Component, OnInit, Input } from '@angular/core';
import { ProgramPerson } from '@app/_models';
import { ActivatedRoute } from '@angular/router';
import { ScheduleService } from '@app/_services/schedule.service';
import { pluck, flatMap, map } from 'rxjs/operators'
import { Observable } from 'rxjs';

@Component({
  selector: 'app-person',
  templateUrl: './person.component.html',
  styleUrls: ['./person.component.scss']
})
export class PersonComponent implements OnInit {
  person?: ProgramPerson;
  loading = false;

  constructor(private route: ActivatedRoute,
    private scheduleService: ScheduleService) { }

  ngOnInit(): void {
    this.loading = true;
    this.route.params.pipe(
      pluck('id'),
      flatMap(id => this.scheduleService.get_person(id)),
    ).subscribe(person => {
      this.loading = false;
      this.person = person;
      if (this.person) {
        this.person.items = {};
        this.scheduleService.get_schedule({id: this.person.prog}).subscribe(items => {
          if (this.person && this.person.items) {
            this.person.items[items[0]] = this.person.items[items[0]] || {};
            this.person.items[items[0]][items[1]] = items[2];
          }
        });
      }
    });
  }

}
