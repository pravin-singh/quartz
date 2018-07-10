import { Component, OnInit, OnDestroy, ViewChild } from "@angular/core";
import { FormGroup, FormControl, FormBuilder, Validators } from '@angular/forms';
import { Router } from "@angular/router";
import { Observable, Subscription } from 'rxjs/Rx';
import { SchedulerService } from "../../services/scheduler.service";
import { ServerResponseCode } from "../../constant/response.code.constants";
import { Task } from "../../modal/task.model";
import { AlertCenterService, AlertType, Alert } from 'ng2-alert-center';



@Component({
  selector: 'new-job',
  templateUrl: './new.component.html',
  styleUrls: ['./new.component.css']
})

export class NewComponent implements OnInit, OnDestroy {
  schedulerForm: FormGroup;
  jobNameStatus: String;
  jobRecords = [];
  jobRefreshTimerSubscription: Subscription;

  isEditMode: boolean = false;
  public loading = false;
  startTask: String = '';
  tasks = [Task];
  taskKey: String = '';
  envStatus: boolean = false;

  cronFlag: boolean = false;
  selectCron;

  constructor(public _router: Router,
    public _fb: FormBuilder,
    public _schedulerService: SchedulerService,
    public _responseCode: ServerResponseCode,
    public _alertService: AlertCenterService) { }

  get jobName(): FormControl {
    return this.schedulerForm.get('jobName') as FormControl;
  }

  get selectCronVal(): FormControl {
    return this.schedulerForm.get('selectCron') as FormControl;
  }

  get year(): FormControl {
    return this.schedulerForm.get('year') as FormControl;
  }

  get month(): FormControl {
    return this.schedulerForm.get('month') as FormControl;
  }

  get day(): FormControl {
    return this.schedulerForm.get('day') as FormControl;
  }
  get minute(): FormControl {
    return this.schedulerForm.get('minute') as FormControl;
  }
  get hour(): FormControl {
    return this.schedulerForm.get('hour') as FormControl;
  }

  ngOnInit() {
    this.loading = true;
    this.jobNameStatus = "";

    this.schedulerForm = this._fb.group({
      jobName: [''],
      year: [''],
      month: [''],
      day: [''],
      hour: [''],
      minute: [''],
      selectCron: [''],
      cronExpression: ['0 0/5 * 1/1 * ? *'],
      startTask: [''],
      scheduleType: ['oneTimeSchedule'],
    });
    this.setDate();
    this.getAllTask();
    this.loading = false;
  }

  ngOnDestroy() {
  }

  setDate(): void {

    //EST offset
    var offset = -4.0;

    var clientDate = new Date();
    var utc = clientDate.getTime() + (clientDate.getTimezoneOffset() * 60000);
    var serverDate = new Date(utc + (3600000 * offset));


    this.schedulerForm.patchValue({
      year: serverDate.getFullYear(),
      month: serverDate.getMonth() + 1,
      day: serverDate.getDate(),
      hour: serverDate.getHours(),
      minute: serverDate.getMinutes()
    });
  }

  resetForm() {
    this.loading = true;

    //EST offset
    var offset = -4.0;

    var clientDate = new Date();
    var utc = clientDate.getTime() + (clientDate.getTimezoneOffset() * 60000);
    var serverDate = new Date(utc + (3600000 * offset));
    this.schedulerForm.patchValue({
      jobName: '',
      year: serverDate.getFullYear(),
      month: serverDate.getMonth() + 1,
      day: serverDate.getDate(),
      hour: serverDate.getHours(),
      minute: serverDate.getMinutes(),
      startTask: '',
      taskKey: '',
      selectCron: '',
      scheduleType: 'oneTimeSchedule'
    });
    this.jobNameStatus = "";
    this.cronFlag = false;
    this.loading = false;
	this.schedulerForm.markAsPristine();
  }

  getJobs() {
    this.loading = true;
    this._schedulerService.getJobs().subscribe(
      success => {
        if (success.statusCode == ServerResponseCode.SUCCESS) {
          this.jobRecords = success.data;
          this.loading = false;
        } else {
          alert("Some error while fetching jobs");
          this.loading = false;
        }
      },
      err => {
        const alert = new Alert(AlertType.DANGER, '', 'Error while getting all jobs.', 5000, true);
        this._alertService.alert(alert);
        this.loading = false;
      });
  }

  getAllTask() {
    this.loading = true;
    this._schedulerService.getAllTask().subscribe(
      success => {
        if (success.statusCode == ServerResponseCode.SUCCESS) {
          this.tasks = success.data;
          this.loading = false;
        } else {
          alert("Some error while fetching jobs");
          this.loading = false;
        }

      },
      err => {
        const alert = new Alert(AlertType.DANGER, '', 'Error while getting the Task List.', 5000, true);
        this._alertService.alert(alert);
        this.loading = false;
      });

  }


  getFormattedDate(year, month, day, hour, minute) {
    return year + "/" + month + "/" + day + " " + hour + ":" + minute;
  }

  scheduleJob() {
    var jobName = this.schedulerForm.value.jobName;
    var year = this.schedulerForm.value.year;
    var month = this.schedulerForm.value.month;
    var day = this.schedulerForm.value.day;
    var hour = this.schedulerForm.value.hour;
    var minute = this.schedulerForm.value.minute;
    var data;

    if (this.cronFlag == false) {
      data = {
        "jobName": this.schedulerForm.value.jobName,
        "jobScheduleTime": this.getFormattedDate(year, month, day, hour, minute),
        "cronExpression": '',
        "startTask": this.startTask,
        "taskName": this.taskKey
      }
    } else {
      data = {
        "jobName": this.schedulerForm.value.jobName,
        "jobScheduleTime": this.getFormattedDate(year, month, day, hour, minute),
        "cronExpression": this.schedulerForm.value.cronExpression,
        "startTask": this.startTask,
        "taskName": this.taskKey
      }
    }

    this.loading = true;
    this._schedulerService.scheduleJob(data).subscribe(
      success => {
        if (success.statusCode == ServerResponseCode.SUCCESS) {
          this.loading = false;
          const alert = new Alert(AlertType.SUCCESS, '', 'Job scheduled successfully.', 5000, true);
          this._alertService.alert(alert);
          this.resetForm();

        } else if (success.statusCode == ServerResponseCode.TASK_IS_ALREADY_SHEDULE) {
          this.loading = false;
          const alert = new Alert(AlertType.DANGER, '', 'Task is already schedule.', 5000, true);
          this._alertService.alert(alert);
        } else if (success.statusCode == ServerResponseCode.JOB_WITH_SAME_NAME_EXIST) {
          this.loading = false;
          const alert = new Alert(AlertType.DANGER, '', 'Job with same name exists, Please choose different name.', 5000, true);
          this._alertService.alert(alert);
        } else if (success.statusCode == ServerResponseCode.JOB_NAME_NOT_PRESENT) {
          this.loading = false;
          const alert = new Alert(AlertType.DANGER, '', 'Job name is mandatory.', 5000, true);
          this._alertService.alert(alert);
        } else if (success.statusCode == ServerResponseCode.JOB_TASK_NOT_PRESENT) {
          this.loading = false;
          const alert = new Alert(AlertType.DANGER, '', 'Job task is mandatory.', 5000, true);
          this._alertService.alert(alert);
        }
        this.jobRecords = success.data;
      },
      err => {
        this.loading = false;
        const alert = new Alert(AlertType.DANGER, '', 'Error while getting all jobs', 5000, true);
        this._alertService.alert(alert);
      });

  }

  refreshJob() {
    this.getJobs();
  }

  cronChange(cronExp) {
    this.loading = true;
    this.schedulerForm.patchValue({
      cronExpression: cronExp
    });
    this.loading = false;
  }

  cancelEdit() {
    this.loading = true;
    this.resetForm();
    this.isEditMode = false;
    this.loading = false;
  }

  startTaskChange(taskValue) {
    var temp = new Array();
    temp = taskValue.split(",");
    this.startTask = temp[0];
    this.taskKey = temp[1];
  }

}