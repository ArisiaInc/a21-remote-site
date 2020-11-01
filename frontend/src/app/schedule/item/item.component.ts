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

  constructor() { }

  ngOnInit(): void {
    console.log(this.item);
  }

  toggleExpand() {
    this.expanded = !this.expanded
  }

}
