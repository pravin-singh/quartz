import { ModuleWithProviders }  from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { NewComponent } from './components/new/new.component';
import { EditComponent } from './components/edit/edit.component';
import { JobsComponent } from './components/jobs/jobs.component';


const appRoutes: Routes = [
  { path: 'newJob', component: NewComponent },
   { path: 'editJobParam',   component: EditComponent },
  { path: 'jobs',   component: JobsComponent }
];

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);