import { Component, OnInit, ViewChild } from "@angular/core";
import { FormGroup, FormControl, FormBuilder, Validators } from '@angular/forms';
import { Router } from "@angular/router";
import { Observable, Subscription } from 'rxjs/Rx';
import { SchedulerService } from "../../services/scheduler.service";
import { ServerResponseCode } from "../../constant/response.code.constants";
import { AlertCenterService, AlertType, Alert } from 'ng2-alert-center';

@Component({
  templateUrl: './jobs.component.html',
  styleUrls: ['./jobs.component.css']
})
export class JobsComponent implements OnInit {
  schedulerForm: FormGroup;
  jobNameStatus: String;
  jobRecords = [];
  jobRefreshTimerSubscription: Subscription;
  public timer;
  public loading = false;

  isEditMode: boolean = false;
  jobFlag:boolean = false;

  constructor(private _router: Router,
    private _fb: FormBuilder,
    private _schedulerService: SchedulerService,
    private _responseCode: ServerResponseCode,
    public _alertService: AlertCenterService) { }

  ngOnInit():void {
    this.getJobs();
  }

  setDate(): void {
    let date = new Date();
    this.schedulerForm.patchValue({
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      day: date.getDate(),
      hour: date.getHours(),
      minute: date.getMinutes()
    });
  }

  resetForm() {
    this.loading = true;
    var dateNow = new Date();
    this.schedulerForm.patchValue({
      jobName: "",
      year: dateNow.getFullYear(),
      month: dateNow.getMonth() + 1,
      day: dateNow.getDate(),
      hour: dateNow.getHours(),
      minute: dateNow.getMinutes()
    });
    this.jobNameStatus = "";
    this.loading = false;
  }

  getJobs() {
    this.loading = true;
    this._schedulerService.getJobs().subscribe(
      success => {
        if (success.statusCode == ServerResponseCode.SUCCESS) {
          this.jobRecords = success.data;
          this.loading = false;
          var jobLength = this.jobRecords.length;
          console.log("Jobs Length"+jobLength);
      
          if(jobLength > 0){
            this.jobFlag = true;
          }
        } else {
          alert("Some error while fetching jobs");
        }
      },
      err => {
        const alert = new Alert(AlertType.DANGER, '', 'Error while getting all jobs.', 5000, true);
        this._alertService.alert(alert);
        this.loading = false;
      });

      
  }

  getFormattedDate(year, month, day, hour, minute) {
    return year + "/" + month + "/" + day + " " + hour + ":" + minute;
  }

  checkJobExistWith(jobName) {
    var data = {
      "jobName": jobName
    }
    this._schedulerService.isJobWithNamePresent(data).subscribe(
      success => {
        if (success.statusCode == ServerResponseCode.SUCCESS) {
          if (success.data == true) {
            this.jobNameStatus = "Bad :(";
          } else {
            this.jobNameStatus = "Good :)";
          }
        } else if (success.statusCode == ServerResponseCode.JOB_NAME_NOT_PRESENT) {
          const alert = new Alert(AlertType.DANGER, '', 'Job name is mandatory.', 5000, true);
          this._alertService.alert(alert);
          this.schedulerForm.patchValue({
            jobName: "",
          });
        }
      },
      err => {
        const alert = new Alert(AlertType.DANGER, '', 'Error while checking job with name exist.', 5000, true);
        this._alertService.alert(alert);
      });
    this.jobNameStatus = "";
  }

  scheduleJob() {
    var jobName = this.schedulerForm.value.jobName;
    var year = this.schedulerForm.value.year;
    var month = this.schedulerForm.value.month;
    var day = this.schedulerForm.value.day;
    var hour = this.schedulerForm.value.hour;
    var minute = this.schedulerForm.value.minute;

    var data = {
      "jobName": this.schedulerForm.value.jobName,
      "jobScheduleTime": this.getFormattedDate(year, month, day, hour, minute),
      "cronExpression": this.schedulerForm.value.cronExpression,
    }
    this.loading = true;
    this._schedulerService.scheduleJob(data).subscribe(
      success => {
        if (success.statusCode == ServerResponseCode.SUCCESS) {
          this.loading = false;
          const alert = new Alert(AlertType.SUCCESS, 'Job scheduled successfully.', '', 5000, true);
          this._alertService.alert(alert);
          this.resetForm();

        } else if (success.statusCode == ServerResponseCode.JOB_WITH_SAME_NAME_EXIST) {
          alert("Job with same name exists, Please choose different name.");

        } else if (success.statusCode == ServerResponseCode.JOB_NAME_NOT_PRESENT) {
          alert("Job name is mandatory.");
        }
        this.jobRecords = success.data;
      },
      err => {
        const alert = new Alert(AlertType.DANGER, '', 'Error while getting all jobs', 5000, true);
        this._alertService.alert(alert);
      });
  }

  updateJob() {
    var jobName = this.schedulerForm.value.jobName;
    var year = this.schedulerForm.value.year;
    var month = this.schedulerForm.value.month;
    var day = this.schedulerForm.value.day;
    var hour = this.schedulerForm.value.hour;
    var minute = this.schedulerForm.value.minute;

    var data = {
      "jobName": this.schedulerForm.value.jobName,
      "jobScheduleTime": this.getFormattedDate(year, month, day, hour, minute),
      "cronExpression": this.schedulerForm.value.cronExpression
    }
    this.loading = true;
    this._schedulerService.updateJob(data).subscribe(
      success => {
        if (success.statusCode == ServerResponseCode.SUCCESS) {
          this.loading = false;

          const alert = new Alert(AlertType.INFO, '', 'Job updated successfully.', 5000, true);
          this._alertService.alert(alert);
          this.resetForm();

        } else if (success.statusCode == ServerResponseCode.JOB_DOESNT_EXIST) {
          const alert = new Alert(AlertType.DANGER, '', 'Job no longer exist.', 5000, true);
          this._alertService.alert(alert);
          this.loading = false;

        } else if (success.statusCode == ServerResponseCode.JOB_NAME_NOT_PRESENT) {
          const alert = new Alert(AlertType.DANGER, '', 'Please provide job name.', 5000, true);
          this._alertService.alert(alert);
          this.loading = false;
        }
        this.jobRecords = success.data;
      },
      err => {
        alert("Error while updating job");
        this.loading = false;
      });
  }

  editJob(selectedJobRow) {
    this._router.navigate(['./editJobParam'], { queryParams: selectedJobRow });
  }


  cancelEdit() {
    this.resetForm();
    this.isEditMode = false;
  }

  pauseJob(jobName, groupName) {
    var data = {
      "jobName": jobName,
      "groupName": groupName
    }
    this.loading = true;
    this._schedulerService.pauseJob(data).subscribe(
      success => {
        if (success.statusCode == ServerResponseCode.SUCCESS && success.data == true) {
          const alert = new Alert(AlertType.DANGER, '', 'Job paused successfully.', 5000, true);
          this._alertService.alert(alert);
          this.loading = false;
        } else if (success.data == false) {
          if (success.statusCode == ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE) {
            const alert = new Alert(AlertType.DANGER, '', 'Job already started/completed, so cannot be paused.', 5000, true);
            this._alertService.alert(alert);
          }
        }
        this.getJobs();
      },
      err => {
        const alert = new Alert(AlertType.DANGER, '', 'Error while pausing job', 5000, true);
        this._alertService.alert(alert);
      });

    //For updating fresh status of all jobs 
    this.getJobs();
  }

  resumeJob(jobName, groupName) {
    var data = {
      "jobName": jobName,
      "groupName": groupName
    }
    this.loading = true;
    this._schedulerService.resumeJob(data).subscribe(
      success => {
        if (success.statusCode == ServerResponseCode.SUCCESS && success.data == true) {
          const alert = new Alert(AlertType.INFO, '', 'Job resumed successfully.', 5000, true);
          this._alertService.alert(alert);
          this.loading = false;
        } else if (success.data == false) {
          if (success.statusCode == ServerResponseCode.JOB_NOT_IN_PAUSED_STATE) {
          const alert = new Alert(AlertType.INFO, '', 'Job is not in paused state, so cannot be resumed.', 5000, true);
          this._alertService.alert(alert);
          this.loading = false;
          }
        }
        this.getJobs();
      },
      err => {
        const alert = new Alert(AlertType.INFO, '', 'Error while resuming job', 5000, true);
        this._alertService.alert(alert);
        this.loading = false;
      });
    this.getJobs();
  }


  startJobNow(jobName, groupName) {
    var data = {
      "jobName": jobName,
      "groupName": groupName
    }
    this.loading = true;
    this._schedulerService.startJobNow(data).subscribe(
      success => {
        if (success.statusCode == ServerResponseCode.SUCCESS && success.data == true) {
          const alert = new Alert(AlertType.INFO, '', 'Job started successfully.', 5000, true);
          this._alertService.alert(alert);
          this.loading = false;
        } else if (success.data == false) {
          if (success.statusCode == ServerResponseCode.ERROR) {
            const alert = new Alert(AlertType.DANGER, '', 'Server error while starting job.', 5000, true);
            this._alertService.alert(alert);
            this.loading = false;
          } else if (success.statusCode == ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE) {
            const alert = new Alert(AlertType.DANGER, '', 'Job is already started.', 5000, true);
            this._alertService.alert(alert);
            this.loading = false;
          } else if (success.statusCode == ServerResponseCode.JOB_DOESNT_EXIST) {
            const alert = new Alert(AlertType.DANGER, '', 'Job no longer exist.', 5000, true);
            this._alertService.alert(alert);
            this.loading = false;
          }
        }
        this.getJobs();
      },
      err => {
        const alert = new Alert(AlertType.DANGER, '', 'Error while starting job now.', 5000, true);
            this._alertService.alert(alert);
            this.loading = false;
      });
    this.getJobs();
  }

  deleteJob(jobName, groupName) {
    var data = {
      "jobName": jobName,
      "groupName": groupName
    }
    this.loading = true;
    this._schedulerService.deleteJob(data).subscribe(
      success => {
        if (success.statusCode == ServerResponseCode.SUCCESS && success.data == true) {
          const alert = new Alert(AlertType.INFO, '', 'Job deleted successfully.', 5000, true);
          this._alertService.alert(alert);
          this.loading = false;
        } else if (success.data == false) {
          if (success.statusCode == ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE) {
            const alert = new Alert(AlertType.INFO, '', 'Job is already started/completed, so cannot be deleted.', 5000, true);
            this._alertService.alert(alert);
            this.loading = false;
          } else if (success.statusCode == ServerResponseCode.JOB_DOESNT_EXIST) {
            const alert = new Alert(AlertType.INFO, '', 'Job no longer exist.', 5000, true);
            this._alertService.alert(alert);
            this.loading = false;
          }
        }

        //For updating fresh status of all jobs
        this.getJobs();
      },
      err => {
        this.loading = false;
      });
  }

  refreshJob() {
    //For updating fresh status of all jobs 
    this.getJobs();
  }

  cronChange(cronExp) {
    this.schedulerForm.patchValue({
      cronExpression: cronExp
    });
  }

}