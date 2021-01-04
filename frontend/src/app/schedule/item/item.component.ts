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
  track!: string;
  type!: string;

  constructor() { }

  ngOnInit(): void {
    this.track = this.event.tags.filter(s => s.startsWith('track'))[0].split(':')[1];
    this.type = this.event.tags.filter(s => s.startsWith('type'))[0].split(':')[1];
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
