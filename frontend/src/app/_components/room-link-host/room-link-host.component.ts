import { Component, OnInit, Input } from '@angular/core';
import { AccountService } from '@app/_services';
import { environment } from "@environments/environment";

@Component({
  selector: 'app-room-link-host',
  templateUrl: './room-link-host.component.html',
  styleUrls: ['./room-link-host.component.scss']
})
export class RoomLinkHostComponent implements OnInit {
  @Input() id!: string;
  isZoomHost: boolean | undefined = false;
  environment = environment;

  constructor(private accountService: AccountService) { }

  ngOnInit(): void {
    this.isZoomHost = this.accountService.user?.zoomHost;
  }

}
