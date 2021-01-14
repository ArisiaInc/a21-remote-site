import { Component, OnInit, Input } from '@angular/core';
import { Ribbon } from '@app/_models/ribbon';

@Component({
  selector: 'app-ribbon',
  templateUrl: './ribbon.component.html',
  styleUrls: ['./ribbon.component.scss']
})
export class RibbonComponent implements OnInit {
  @Input() ribbon!: Ribbon;

  constructor() { }

  ngOnInit(): void {
  }

}
