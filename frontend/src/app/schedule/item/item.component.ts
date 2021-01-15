import { Component, OnChanges, Input } from '@angular/core';
import { ScheduleService, ScheduleEvent } from '@app/_services';

@Component({
  selector: 'app-item',
  templateUrl: './item.component.html',
  styleUrls: ['./item.component.scss']
})
export class ItemComponent implements OnChanges {
  @Input() event!: ScheduleEvent;
  @Input() showStar = false;
  @Input() openDoors!: Set<string>;
  showDoors = false;
  expanded: boolean = false;
  track!: string;
  type!: string;

  constructor() { }

  ngOnChanges(): void {
    this.showDoors = this.openDoors && this.openDoors.has(this.event.id);
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
