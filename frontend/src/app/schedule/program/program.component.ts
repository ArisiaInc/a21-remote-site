import { Component, OnInit, Input } from '@angular/core';
import { ProgramItem } from '@app/_models';
import { ScheduleService } from '@app/_services/schedule.service';

@Component({
  selector: 'app-program',
  templateUrl: './program.component.html',
  styleUrls: ['./program.component.scss']
})
export class ProgramComponent implements OnInit {
  items: {[_:string]: {[_:string]: ProgramItem[]}} = {};

  constructor(private scheduleService: ScheduleService) { }

  // TODO allow filtering by url params
  ngOnInit(): void {
    this.scheduleService.get_schedule().subscribe(items => {
      this.items[items[0]] = this.items[items[0]] || {}
      this.items[items[0]][items[1]] = items[2]
    });
  }

  updateItems(filters) {
    console.log('updating')
    this.items = {}
    this.scheduleService.get_schedule(filters).subscribe(items => {
      this.items[items[0]] = this.items[items[0]] || {}
      this.items[items[0]][items[1]] = items[2]
    });
  }

}
