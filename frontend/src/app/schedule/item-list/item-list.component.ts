import { Component, OnInit, Input } from '@angular/core';
import { ProgramItem } from '@app/_models';

@Component({
  selector: 'app-item-list',
  templateUrl: './item-list.component.html',
  styleUrls: ['./item-list.component.scss']
})
export class ItemListComponent implements OnInit {
  @Input() items: {[_:string]: {[_:string]: ProgramItem[]}} = {};

  constructor() { }

  ngOnInit(): void {
  }

}
