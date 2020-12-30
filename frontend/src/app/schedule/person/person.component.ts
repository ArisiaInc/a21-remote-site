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
  person$!: Observable<ProgramPerson | undefined>;

  constructor(private route: ActivatedRoute,
    private scheduleService: ScheduleService) { }

  ngOnInit(): void {
    this.person$ = this.route.params.pipe(
      pluck('id'),
      flatMap(id => this.scheduleService.get_person_with_items(id)),
    );
  }

}
