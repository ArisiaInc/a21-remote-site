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
  @Input() alwaysExpanded = false;
  showDoors = false;
  expanded: boolean = false;

  constructor() { }

  ngOnChanges(): void {
    this.showDoors = this.openDoors && this.openDoors.has(this.event.id);
    if (this.alwaysExpanded) {
      this.expanded = true;
    }
  }

  toggleExpand(event: Event) {
    if (!this.alwaysExpanded) {
      this.expanded = !this.expanded;
    }
    event.stopPropagation();
  }

  onStarClick(event: Event) {
    this.event.starred = !this.event.starred
    event.stopPropagation();
  }

}
