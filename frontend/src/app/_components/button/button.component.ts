import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';

@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss']
})
export class ButtonComponent implements OnInit {
  @Output()
  clicked = new EventEmitter<string>();


  constructor() { }

  ngOnInit(): void {
  }

  onClick() {
    this.clicked.emit('clicked');
  }

}
