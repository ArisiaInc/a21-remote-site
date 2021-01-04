import { Component, OnInit, Input } from '@angular/core';
import { Creator } from '@app/_models';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-creator-list',
  templateUrl: './creator-list.component.html',
  styleUrls: ['./creator-list.component.scss']
})
export class CreatorListComponent implements OnInit {
  @Input() creators: Creator[] = [];

  constructor() { }

  ngOnInit(): void {
  }

}
