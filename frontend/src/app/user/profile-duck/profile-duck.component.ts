import { Component, OnInit, Input } from '@angular/core';
import { Duck } from '@app/_models';

@Component({
  selector: 'app-profile-duck',
  templateUrl: './profile-duck.component.html',
  styleUrls: ['./profile-duck.component.scss']
})
export class ProfileDuckComponent implements OnInit {
  @Input() duck!: Duck;
  @Input() isSelf: boolean = false
  @Input() hidden: boolean = false

  constructor() { }

  ngOnInit(): void {
  }

}
