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


  constructor() { }

  ngOnInit(): void {
    console.log("button", this)
  }

  onClick() {
    this.clicked.emit('clicked');
  }

}
