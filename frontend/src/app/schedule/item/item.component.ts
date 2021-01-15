import { Component, OnInit, Input } from '@angular/core';
import { ScheduleEvent } from '@app/_services';

@Component({
  selector: 'app-item',
  templateUrl: './item.component.html',
  styleUrls: ['./item.component.scss']
})
export class ItemComponent implements OnInit {
  @Input() event!: ScheduleEvent;
  @Input() showStar = false;
  expanded: boolean = false;

  constructor() { }

  ngOnInit(): void {
  }

  toggleExpand(event: Event) {
    this.expanded = !this.expanded;
    event.stopPropagation();
  }

  onStarClick(event: Event) {
    this.event.starred = !this.event.starred
    event.stopPropagation();
  }

}
