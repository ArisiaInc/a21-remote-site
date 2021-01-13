import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { AuthGuard } from '@app/_helpers';
import { ProgramComponent } from './program/program.component';
import { PersonComponent } from './person/person.component';
import { LayoutComponent } from './layout/layout.component';
import { PeopleComponent } from './people/people.component';
import { StarredComponent } from './starred/starred.component';

const routes: Routes = [
  {path: '', component: LayoutComponent,
   children: [
     { path: '', redirectTo: 'program', pathMatch: 'full' },
     { path: 'starred', component: StarredComponent, canActivate: [AuthGuard] },
     { path: 'people/search/:search', component: PeopleComponent },
     { path: 'people', component: PeopleComponent },
     { path: 'program', component: ProgramComponent },
   ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ScheduleRoutingModule { }
