import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Creator } from '@app/_models';
import { ActivatedRoute } from '@angular/router';
import { CreatorService } from '@app/_services';
import { pluck, map, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-dealer',
  templateUrl: './dealer.component.html',
  styleUrls: ['./dealer.component.scss']
})
export class DealerComponent implements OnInit {
  dealer$!: Observable<Creator | undefined>;

  constructor(private route: ActivatedRoute,
    private creatorService: CreatorService) { }

  ngOnInit(): void {
    this.dealer$ = this.route.params.pipe(
      pluck('id'),
      switchMap(id => this.creatorService.get_dealers([id])),
      map(x => x.length ? x[0] : undefined)
    )
  }

}
