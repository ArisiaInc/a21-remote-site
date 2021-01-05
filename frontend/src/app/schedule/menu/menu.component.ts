import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { StarsService } from '@app/_services';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss']
})
export class MenuComponent implements OnInit {
  constructor(public starsService: StarsService) {
  }

  ngOnInit(): void {
  }

}
