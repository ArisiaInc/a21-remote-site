import { Component, OnInit, Input } from '@angular/core';
import { ProgramFilter, ProgramItem } from '@app/_models';
import { ScheduleService, StructuredScheduleItems } from '@app/_services/schedule.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-program',
  templateUrl: './program.component.html',
  styleUrls: ['./program.component.scss']
})
export class ProgramComponent implements OnInit {
  items$!: Observable<StructuredScheduleItems>;

  constructor(private scheduleService: ScheduleService) { }

  // TODO allow filtering by url params
  ngOnInit(): void {
    this.items$ = this.scheduleService.get_schedule();
  }

  updateItems(filters: ProgramFilter) {
    console.log('updating')
    this.items$ = this.scheduleService.get_schedule(filters);
  }

}
