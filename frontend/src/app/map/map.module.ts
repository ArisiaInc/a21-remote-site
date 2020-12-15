import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MapRoutingModule } from './map-routing.module';
import { MapComponent } from './map.component';
import { SharedModule } from '@app/_components';
import { FeaturedEventsComponent } from './featured-events/featured-events.component';


@NgModule({
  declarations: [MapComponent, FeaturedEventsComponent],
  imports: [
    CommonModule,
    MapRoutingModule,
    SharedModule
  ]
})
export class MapModule { }
