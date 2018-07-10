import { NgModule }                 from '@angular/core';
import { Routes, RouterModule }     from '@angular/router';
import { HashLocationStrategy, LocationStrategy } from '@angular/common';
import { BrowserModule }            from '@angular/platform-browser';
import { routing }                from './app.routes'; 
import { CommonModule }                     from '@angular/common';
import { HttpModule }                       from '@angular/http';
import { LoadingModule } from 'ngx-loading';
import{ ReactiveFormsModule,FormsModule } from '@angular/forms'; 
import 'rxjs/add/operator/map'
import { AppComponent } from './app.component';
import { SchedulerModule } from './services/scheduler.module';
import { NewComponent } from './components/new/new.component';
import { NavComponent } from './components/nav/nav.component';
import { JobsComponent } from './components/jobs/jobs.component';
import { EditComponent } from './components/edit/edit.component';
import { AlertCenterModule } from 'ng2-alert-center';
import { AlertCenterService } from 'ng2-alert-center';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';


@NgModule({
    //put all your modules here
    //The imports key in the context of an @NgModule defines additional modules 
    //that will be imported into the current module
    imports: [ 
		BrowserModule,
	  SchedulerModule,
		routing,
		ReactiveFormsModule, FormsModule, CommonModule,HttpModule,LoadingModule,AlertCenterModule,BrowserAnimationsModule
	],
    // put all your components / directives / pipes here
   declarations: [ AppComponent, JobsComponent,
    NewComponent, NavComponent, EditComponent],

    // put all your services here
    providers: [ 
      { provide: LocationStrategy, useClass: HashLocationStrategy},AlertCenterService
    ],
  
  bootstrap: [AppComponent]
})
export class AppModule { }


