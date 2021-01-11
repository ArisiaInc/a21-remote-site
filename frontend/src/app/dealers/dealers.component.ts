import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { pluck, map, switchMap } from 'rxjs/operators';

import { Creator } from '@app/_models';
import { DealerService } from '@app/_services';

@Component({
  selector: 'app-dealers',
  templateUrl: './dealers.component.html',
  styleUrls: ['./dealers.component.scss']
})
export class DealersComponent implements OnInit {
  dealers$!: Observable<Creator[]>;
  search$!: Observable<string>;

  constructor(
    private route: ActivatedRoute,
    public dealerService: DealerService) {}

  ngOnInit(): void {
    this.search$ = this.route.params.pipe(
      pluck('search'),
      map(search => (search || '').toLowerCase())
    );

    this.dealers$ = this.search$.pipe(
      switchMap(search => this.dealerService.search(search)),
    );
  }
}
