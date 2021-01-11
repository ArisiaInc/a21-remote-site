import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { ScheduleService, RunningEvents } from '@app/_services';

@Component({
  selector: 'app-performance',
  templateUrl: './performance.component.html',
  styleUrls: ['./performance.component.scss']
})
export class PerformanceComponent implements OnInit {
  events$: Observable<RunningEvents>;
  url$: Observable<SafeUrl | undefined>;

  constructor(private scheduleService: ScheduleService,
              private sanitizer: DomSanitizer) {
    this.events$ = scheduleService.getPerformanceEvents();
    this.url$ = this.events$.pipe(
      map(runningEvents => {
        const performance = runningEvents?.current?.performance;
        if (performance) {
          let url: string | undefined;
          switch (performance.platform) {
            case 'youtubeVideo':
              url = `https://www.youtube.com/embed/${performance.streamId}`;
              break;
            case 'twitchChannel':
              url = `https://player.twitch.tv/?channel=${performance.streamId}&parent=localhost`;
              break;
            case 'twitchVideo':
              url = `https://player.twitch.tv/?video=${performance.streamId}&parent=localhost`;
              break;
            case 'vimeoVideo':
              url = `https://player.vimeo.com/video/${performance.streamId}?autoplay=1&title=0&byline=0&portrait=0`;
              break;
          }
          if (url) {
            return this.sanitizer.bypassSecurityTrustResourceUrl(url);
          }
        }
        return undefined;
      }),
    );
  }

  ngOnInit(): void {
  }

}
