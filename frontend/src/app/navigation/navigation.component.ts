import { ActivatedRoute } from '@angular/router';
import { Component, OnInit, HostBinding } from '@angular/core';


@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss']
})
export class NavigationComponent implements OnInit {
@HostBinding('class') class = 'navbar navbar-expand-md navbar-dark bg-primary row';

  constructor(public route: ActivatedRoute) { }

  ngOnInit(): void {
  }

}
