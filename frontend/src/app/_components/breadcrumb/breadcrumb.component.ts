import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';

export interface Crumb {
  path: string;
  label: string;
}

@Component({
  selector: 'app-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss']
})
export class BreadcrumbComponent implements OnInit {
  crumbs!: Crumb[];
  @Input() crumbsOverride?: Crumb[];
  @Input() lobby = true;
  @Input() lastCrumbText?: string;

  constructor(private router: Router) { }

  ngOnInit(): void {
    if (this.crumbsOverride) {
      this.crumbs = this.crumbsOverride;
    } else {
      const url = this.router.routerState.snapshot.url;
      const pre_crumbs = url.split('/').slice(1);
      this.crumbs = pre_crumbs.map(c => ({path: url.split(c)[0] + c, label: this.crumbToText(c)}))
      if (this.lobby) {
        this.crumbs.unshift({path: '/map', label: 'Lobby'})
      }
      if (this.lastCrumbText) {
        this.crumbs[this.crumbs.length - 1].label = this.lastCrumbText;
      }
    }
  }

  crumbToText(path: string) {
    if (path == 'map') {
      return "Lobby";
    }
    return path.replace(/_/g, ' ');
  }

}
