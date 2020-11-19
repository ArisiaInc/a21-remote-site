import { Component } from '@angular/core';
import { AccountService } from './_services';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'frontend';

  constructor( private accountService: AccountService) {}

  ngOnInit() {
  }
}
