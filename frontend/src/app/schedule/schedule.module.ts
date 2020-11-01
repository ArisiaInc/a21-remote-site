import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ScheduleRoutingModule } from './schedule-routing.module';
import { ProgramComponent } from './program/program.component';
import { PersonComponent } from './person/person.component';
import { ItemComponent } from './item/item.component';
import { LayoutComponent } from './layout/layout.component';
import { MenuComponent } from './menu/menu.component';
import { PeopleComponent } from './people/people.component';


@NgModule({
  declarations: [ProgramComponent, PersonComponent, ItemComponent, LayoutComponent, MenuComponent, PeopleComponent],
  imports: [
    CommonModule,
    ScheduleRoutingModule
  ]
})
export class ScheduleModule { }
