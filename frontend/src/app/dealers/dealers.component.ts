import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { Creator } from '@app/_models';
import { DealerService } from '@app/_services';

@Component({
  selector: 'app-dealers',
  templateUrl: './dealers.component.html',
  styleUrls: ['./dealers.component.scss']
})
export class DealersComponent implements OnInit {
  dealers$!: Observable<Creator[]>;

  constructor(public dealerService: DealerService) {}

  ngOnInit(): void {

    this.dealers$ = this.dealerService.search('');
  }
}
