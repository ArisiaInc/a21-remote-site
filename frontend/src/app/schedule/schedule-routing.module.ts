import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ProgramComponent } from './program/program.component';
import { PersonComponent } from './person/person.component';
import { LayoutComponent } from './layout/layout.component';
import { PeopleComponent } from './people/people.component';
import { StarredComponent } from './starred/starred.component';

const routes: Routes = [
  {path: '', component: LayoutComponent,
   children: [
     {path: 'starred', component: StarredComponent},
     {path: 'people/:search', component: PeopleComponent},
     {path: 'people', component: PeopleComponent},
     {path: 'program', component: ProgramComponent}
   ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ScheduleRoutingModule { }
