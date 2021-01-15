import { Component, OnInit, Input } from '@angular/core';
import { Observable } from 'rxjs';

import { StructuredEvents, StarsService, ScheduleService } from '@app/_services';

@Component({
  selector: 'app-item-list',
  templateUrl: './item-list.component.html',
  styleUrls: ['./item-list.component.scss']
})
export class ItemListComponent implements OnInit {
  @Input() events: StructuredEvents = [];

  constructor(public scheduleService: ScheduleService, public starsService: StarsService) {
  }

  ngOnInit(): void {
  }

}
