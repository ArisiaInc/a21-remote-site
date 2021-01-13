import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-ribbon-list',
  templateUrl: './ribbon-list.component.html',
  styleUrls: ['./ribbon-list.component.scss']
})
export class RibbonListComponent implements OnInit {
  @Input() isSelf: boolean = false;

  constructor() { }

  ngOnInit(): void {
  }

}
