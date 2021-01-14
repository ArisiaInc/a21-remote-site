import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface Link {
  text?: string;
  url: string;
}

interface Credit {
  bio: string;
  possessive: string;
  links: Link[];
  picture: string;
}

@Component({
  selector: 'app-credits',
  templateUrl: './credits.component.html',
  styleUrls: ['./credits.component.scss']
})
export class CreditsComponent implements OnInit {
  credits: Credit[] = [];
  columns: Credit[][] = [];
  error = '';

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.http.get<Credit[]>('/assets/data/credits.json').subscribe(
      response => {
        this.credits = response,
        this.columns[0] = this.credits.slice(0, 4);
        this.columns[1] = this.credits.slice(4);
      },
      error => this.error = 'Failed to load credits data.'
    );
  }

}
