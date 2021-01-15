import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ScheduleRoutingModule } from './schedule-routing.module';
import { ProgramComponent } from './program/program.component';
import { PersonComponent } from './person/person.component';
import { ItemComponent } from './item/item.component';
import { LayoutComponent } from './layout/layout.component';
import { MenuComponent } from './menu/menu.component';
import { PeopleComponent } from './people/people.component';
import { FiltersComponent } from './filters/filters.component';
import { ItemListComponent } from './item-list/item-list.component';
import { PipesModule } from '../pipes/pipes.module';
import { SharedModule } from '../_components';
import { StarredComponent } from './starred/starred.component';
import { PersonPageComponent } from './person-page/person-page.component';


@NgModule({
  declarations: [ProgramComponent, PersonComponent, ItemComponent, LayoutComponent, MenuComponent, PeopleComponent, FiltersComponent, ItemListComponent, StarredComponent, PersonPageComponent],
  providers: [],
  imports: [
    CommonModule,
    ScheduleRoutingModule,
    PipesModule,
    SharedModule,
  ]
})
export class ScheduleModule { }
