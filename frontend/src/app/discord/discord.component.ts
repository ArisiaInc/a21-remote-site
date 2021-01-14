import { Component, OnInit } from '@angular/core';

import { DiscordService } from '@app/_services';

@Component({
  selector: 'app-discord',
  templateUrl: './discord.component.html',
  styleUrls: ['./discord.component.scss']
})
export class DiscordComponent implements OnInit {
  username = '';
  usernameErrors = '';
  loading = false;
  success = false;

  private secret = '';
  secretLoading = false;
  secretDisplayed = false;
  secretError = '';

  constructor(private discordService: DiscordService) { }

  ngOnInit(): void {
  }

  connect() {
    this.loading = true;
    this.usernameErrors = '';
    if (!this.username) {
      this.usernameErrors = "Username and 4 digit code are required";
    }
    this.discordService.addArisian(this.username).subscribe(
      resp => {
        this.success = true;
        this.usernameErrors = '';
        this.loading = false;
      },
      err => {
        this.success = false;
        this.usernameErrors = err || "Internal Error";
        this.loading = false;
      }
    );
  }

  display() {
    if (this.secret) {
      this.secretDisplayed = true;
    } else {
      this.discordService.getAssistSecret().subscribe(
      resp => {
        this.secret = resp;
        this.secretError = '';
        this.secretLoading = false;
        this.secretDisplayed = true;
      },
      err => {
        this.secret = '';
        this.secretError = err;
        this.secretLoading = false;
        this.secretDisplayed = false;
      });
    }
  }

  hide() {
    this.secretDisplayed = false;
  }

}
