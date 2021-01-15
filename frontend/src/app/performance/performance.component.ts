import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { Observable, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';

import { ScheduleService, RunningEvents, SettingsService } from '@app/_services';
import { Performance } from '@app/_models';

@Component({
  selector: 'app-performance',
  templateUrl: './performance.component.html',
  styleUrls: ['./performance.component.scss']
})
export class PerformanceComponent implements OnInit, OnDestroy {
  events$: Observable<RunningEvents>;
  url: SafeUrl | undefined;
  platform: string | undefined;
  subscription: Subscription;
  performance?: Performance;
  runningEvents?: RunningEvents;
  pauses = false;

  @ViewChild('iframe') iframe?: ElementRef<HTMLIFrameElement>;

  constructor(private scheduleService: ScheduleService,
              private settingsService: SettingsService,
              private sanitizer: DomSanitizer) {
    this.events$ = scheduleService.getPerformanceEvents();
    this.subscription = this.events$.subscribe(
      runningEvents => this.setRunningEvents(runningEvents));
  }

  setRunningEvents(runningEvents: RunningEvents) {
    this.runningEvents = runningEvents;
    const performance = runningEvents?.current?.performance;
    if (this.performance !== performance) {
      let url: string | undefined;
      if (performance) {
        this.url = undefined;
        this.platform = undefined;
        this.pauses = false;

        let params: string[] = [];
        let paramString: string = '';
        let autoPlay = !this.settingsService.disableAnimations;
        const offset = runningEvents.current ? this.settingsService.currentTime.getTime() - runningEvents.current.start.getTime() : 0;
        switch (performance.platform) {
          case 'youtubeVideo':
            params.push('enablejsapi=1');
            if (autoPlay) {
              params.push('autoplay=1');
            }
            if (offset > 0) {
              params.push(`start=${Math.floor(offset / 1000)}`);
            }
            paramString = params.length > 0 ? '?' + params.join('&') : '';
            url = `https://www.youtube.com/embed/${performance.streamId}${paramString}`;
            this.platform = "YouTube";
            this.pauses = true;
            break;
          case 'twitchChannel':
            if (autoPlay) {
              params.push('autoplay=true');
            } else {
              params.push('autoplay=false');
            }
            paramString = params.length > 0 ? '&' + params.join('&') : '';
            url = `https://player.twitch.tv/?channel=${performance.streamId}&parent=localhost${paramString}`;
            this.platform = "Twitch";
            break;
          case 'twitchVideo':
            if (autoPlay) {
              params.push('autoplay=true');
            } else {
              params.push('autoplay=false');
            }
            if (offset > 0) {
              params.push(`time=${Math.floor(offset / 1000)}s`);
            }
            paramString = params.length > 0 ? '&' + params.join('&') : '';
            url = `https://player.twitch.tv/?video=${performance.streamId}&parent=localhost${paramString}`;
            this.platform = "Twitch";
            break;
          case 'vimeoVideo':
            if (autoPlay) {
              params.push('autoplay=1');
            }
            let hash;
            if (offset > 0) {
              hash = `#t=${Math.floor(offset / 1000)}s`;
            }
            paramString = params.length > 0 ? '&' + params.join('&') : '';
            url = `https://player.vimeo.com/video/${performance.streamId}?title=0&byline=0&portrait=0${paramString}${hash}`;
            this.platform = "Vimeo";
            this.pauses = true;
            break;
        }
      }
      if (url) {
        this.url = this.sanitizer.bypassSecurityTrustResourceUrl(url);
      }
      this.performance = performance;
    }
  }

  pauseYouTube(): void {
    const iframe = this.iframe?.nativeElement;
    if (iframe && iframe.contentWindow) {
      iframe.contentWindow.postMessage('{"event":"command","func":"pauseVideo","args":""}', '*')
    }
  }

  openExternal(): void {
    if (this.performance && this.runningEvents) {
      let params: string[] = [];
      let paramString: string = '';
      const offset = this.runningEvents.current ? this.settingsService.currentTime.getTime() - this.runningEvents.current.start.getTime() : 0;
      const autoPlay = !this.settingsService.disableAnimations;
      let externalUrl: string | undefined;
      let hash;

      switch (this.performance.platform) {
        case 'youtubeVideo':
          params.push('enablejsapi=1');
          if (autoPlay) {
            params.push('autoplay=1');
          }
          if (offset > 0) {
            params.push(`start=${Math.floor(offset / 1000)}`);
          }
          paramString = params.length > 0 ? '?' + params.join('&') : '';
          externalUrl = `https://www.youtube.com/watch/${this.performance.streamId}${paramString}`;
          this.pauseYouTube();
          break;
        case 'twitchChannel':
          if (autoPlay) {
            params.push('autoplay=true');
          } else {
            params.push('autoplay=false');
          }
          paramString = params.length > 0 ? '?' + params.join('&') : '';
          externalUrl = `https://twitch.tv/${this.performance.streamId}${paramString}`;
          break;
        case 'twitchVideo':
          if (autoPlay) {
            params.push('autoplay=true');
          } else {
            params.push('autoplay=false');
          }
          if (offset > 0) {
            params.push(`t=${Math.floor(offset / 1000)}s`);
          }
          paramString = params.length > 0 ? '?' + params.join('&') : '';
          externalUrl = `https://twitch.tv/videos/${this.performance.streamId}${paramString}`;
          break;
        case 'vimeoVideo':
          if (autoPlay) {
            params.push('autoplay=1');
          }
          if (offset > 0) {
            hash = `#t=${Math.floor(offset / 1000)}s`;
          }
          paramString = params.length > 0 ? '&' + params.join('&') : '';
          externalUrl = `https://player.vimeo.com/video/${this.performance.streamId}?title=0&byline=0&portrait=0${paramString}${hash}`;
          break;
      }
      window.open(externalUrl, "_blank");
    }
  }

  ngOnInit(): void {
  }

  ngOnDestroy() {
    this.subscription.unsubscribe()
  }
}
