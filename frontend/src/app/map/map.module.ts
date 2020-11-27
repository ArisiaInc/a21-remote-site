import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MapRoutingModule } from './map-routing.module';
import { MapLinkComponent } from './map-link/map-link.component';
import { MapComponent } from './map.component';
import { SharedModule } from '@app/_components';


@NgModule({
  declarations: [MapLinkComponent, MapComponent],
  imports: [
    CommonModule,
    MapRoutingModule,
    SharedModule
  ]
})
export class MapModule { }
