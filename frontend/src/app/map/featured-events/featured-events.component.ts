import { Component, OnInit, HostBinding } from '@angular/core';
import { ScheduleService } from '@app/_services';
import { ProgramItem } from '@app/_models';

@Component({
  selector: 'app-featured-events',
  templateUrl: './featured-events.component.html',
  styleUrls: ['./featured-events.component.scss']
})
export class FeaturedEventsComponent implements OnInit {
  items: ProgramItem[] = [];

  @HostBinding('class') class = 'm-2';

  constructor( private scheduleService: ScheduleService) { }

  ngOnInit(): void {
    this.scheduleService.get_featured_events().subscribe(items => this.items = items);
  }

}
