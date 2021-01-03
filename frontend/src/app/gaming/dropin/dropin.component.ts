import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-dropin',
  templateUrl: './dropin.component.html',
  styleUrls: ['./dropin.component.scss']
})
export class DropinComponent implements OnInit {
  available = false;

  constructor() { }

  ngOnInit(): void {
    // TODO figure out when we will know how to turn this on
  }

}
