import { Component, OnInit, Input } from '@angular/core';
import { ProgramItem } from '@app/_models';

@Component({
  selector: 'app-item',
  templateUrl: './item.component.html',
  styleUrls: ['./item.component.scss']
})
export class ItemComponent implements OnInit {
  @Input() item: ProgramItem;
  expanded: boolean = false;
  track: string;
  type: string;

  constructor() { }

  ngOnInit(): void {
    this.track = this.item.tags.filter(s => s.startsWith('track'))[0].split(':')[1];
    this.type = this.item.tags.filter(s => s.startsWith('type'))[0].split(':')[1];
  }

  toggleExpand() {
    this.expanded = !this.expanded;
  }

}
