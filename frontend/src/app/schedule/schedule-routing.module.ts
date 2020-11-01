import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ProgramComponent } from './program/program.component';
import { PersonComponent } from './person/person.component';
import { LayoutComponent } from './layout/layout.component';
import { PeopleComponent } from './people/people.component';

const routes: Routes = [
  {path: '', component: LayoutComponent,
  children: [
    {path: 'people/:id', component: PersonComponent},
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
