import { Component, OnInit } from '@angular/core';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';

import { SettingsService, AccountService } from '@app/_services';

interface TimeModel {
  hour: number;
  minute: number;
  second: number;
}

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  timeModel_!: TimeModel;
  dateModel_!: NgbDateStruct;
  dev!: boolean;

  get dateModel(): NgbDateStruct {
    return this.dateModel_;
  }

  set dateModel(value: NgbDateStruct) {
    this.dateModel_ = value;
    this.setTimeOffset();
  }

  get timeModel(): TimeModel {
    return this.timeModel_;
  }

  set timeModel(value: TimeModel) {
    this.timeModel_ = value;
    this.setTimeOffset();
  }

  get hour24(): boolean {
    return !this.settingsService.hour12;
  }

  set hour24(value: boolean) {
    this.settingsService.hour12 = !value;
  }

  constructor(public settingsService: SettingsService, public accountService: AccountService) { }

  ngOnInit(): void {
    const now: Date = this.settingsService.currentTime;
    this.timeModel_ = {
      hour: now.getHours(),
      minute: now.getMinutes(),
      second: now.getSeconds(),
    };
    this.dateModel_ = {
      year: now.getFullYear(),
      month: now.getMonth() + 1,
      day: now.getDate(),
    };
    const badgeNumber = this.accountService.user?.badgeNumber;
    this.dev = badgeNumber === '108383' || badgeNumber === '94878';
  }

  setTimeOffset(): void {
    this.settingsService.currentTime = new Date(this.dateModel.year, this.dateModel.month - 1, this.dateModel.day, this.timeModel.hour, this.timeModel.minute, this.timeModel.second);
  }

}
