import { ActivatedRoute } from '@angular/router';
import { Component, OnInit, HostBinding } from '@angular/core';


@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss']
})
export class NavigationComponent implements OnInit {
  @HostBinding('class') class = 'navbar navbar-expand-md navbar-light row sticky-top py-md-0';
  public isMenuCollapsed = true;

  constructor(public route: ActivatedRoute) { }

  ngOnInit(): void {
  }

}
