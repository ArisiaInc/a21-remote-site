import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MapRoutingModule } from './map-routing.module';
import { MapComponent } from './map.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { SharedModule } from '@app/_components';
import { FeaturedEventsComponent } from './featured-events/featured-events.component';
import { HallCostumesComponent } from './hall-costumes/hall-costumes.component';


@NgModule({
  declarations: [MapComponent, FeaturedEventsComponent, HallCostumesComponent],
  imports: [
    CommonModule,
    MapRoutingModule,
    NgbModule,
    SharedModule
  ]
})
export class MapModule { }
