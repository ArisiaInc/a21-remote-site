import { Component, OnInit } from '@angular/core';
import { ClipboardService } from 'ngx-clipboard'
import { Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

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
  showSecretLoading = false;
  secretDisplayed = false;
  secretError = '';
  secretCopied = false;
  secretCopyFailed = false;

  constructor(private discordService: DiscordService, private clipboardService: ClipboardService) { }

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

  load(): Observable<any> {
    this.showSecretLoading = false;
    window.setTimeout(() => this.showSecretLoading = true, 200);
    this.secretLoading = true;
    return this.discordService.getAssistSecret().pipe(
      tap(resp => {
        this.secret = resp;
        this.secretError = '';
        this.secretLoading = false;
      }),
      catchError(err => {
        this.secret = '';
        this.secretError = err;
        this.secretLoading = false;
        return throwError(err);
      }),
    );
  }

  display() {
    if (this.secret) {
      this.secretDisplayed = true;
    } else {
      this.load().subscribe(
        _ => this.secretDisplayed = true,
        _ => this.secretDisplayed = false);
    }
  }

  hide() {
    this.secretDisplayed = false;
  }

  copy() {
    if (this.secret) {
      this.doCopy();
    } else {
      this.load().subscribe(
        _ => this.doCopy(),
        _ => this.secretCopyFailed = true);
    }
  }

  doCopy() {
    this.clipboardService.copy(this.secret)
    this.secretCopied = true;
    setTimeout(() => this.secretCopied = false, 1000);
  }
}
