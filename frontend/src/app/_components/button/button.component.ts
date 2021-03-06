import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';

@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss']
})
export class ButtonComponent implements OnInit {
  @Output()
  clicked = new EventEmitter<string>();
  @Input() primary: boolean = false;
  @Input() toggle: boolean = false;
  @Input() active: boolean = false;
  @Input() small: boolean = false;


  constructor() { }

  ngOnInit(): void {}

}
