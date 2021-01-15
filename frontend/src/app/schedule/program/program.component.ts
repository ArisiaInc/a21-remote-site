import { Component, OnInit, Input } from '@angular/core';
import { ProgramFilter, ProgramItem } from '@app/_models';
import { ScheduleService, StructuredEvents } from '@app/_services/schedule.service';
import { Observable } from 'rxjs';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-program',
  templateUrl: './program.component.html',
  styleUrls: ['./program.component.scss']
})
export class ProgramComponent implements OnInit {
  events$!: Observable<StructuredEvents>;

  constructor(private scheduleService: ScheduleService) { }

  // TODO allow filtering by url params
  ngOnInit(): void {
    this.events$ = this.scheduleService.getSchedule().pipe(take(1));
  }

  updateItems(filters: ProgramFilter) {
    this.events$ = this.scheduleService.getSchedule(filters).pipe(take(1));
  }

}
