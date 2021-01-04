import { Component, OnInit, Input } from '@angular/core';
import { StructuredEvents } from '@app/_services/schedule.service';

@Component({
  selector: 'app-item-list',
  templateUrl: './item-list.component.html',
  styleUrls: ['./item-list.component.scss']
})
export class ItemListComponent implements OnInit {
  @Input() events: StructuredEvents = [];

  constructor() { }

  ngOnInit(): void {
  }

}
