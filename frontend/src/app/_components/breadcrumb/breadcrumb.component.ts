import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss']
})
export class BreadcrumbComponent implements OnInit {
  crumbs!: {path: string, label: string}[];
  @Input() lobby = true;

  constructor(private router: Router) { }

  ngOnInit(): void {
    const url = this.router.routerState.snapshot.url;
    const pre_crumbs = url.split('/').slice(1);
    this.crumbs = pre_crumbs.map(c => ({path: url.split(c)[0] + c, label: this.crumbToText(c)}))
    if (this.lobby) {
      this.crumbs.unshift({path: '/map', label: 'Lobby'})
    }
    console.log("crumbs", this.crumbs)
  }

  crumbToText(path: string) {
    if (path == 'map') {
      return "Lobby";
    }
    return path.replace('_', ' ');
  }

}
