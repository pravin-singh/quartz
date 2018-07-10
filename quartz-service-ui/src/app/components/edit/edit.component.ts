import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormGroup, FormControl, FormBuilder, Validators } from '@angular/forms';
import { SchedulerService } from '../../services/scheduler.service';
import { ServerResponseCode } from '../../constant/response.code.constants';
import { AlertType, Alert, AlertCenterService } from 'ng2-alert-center';

@Component({
  templateUrl: './edit.component.html',
  styleUrls: ['./edit.component.css']
})
export class EditComponent implements OnInit {
  editForm: FormGroup;
  jobNameStatus: String;
  jobRecords = [];
  isEditMode: boolean = false;
  public loading = false;
  cronExpression;
  cronFlag: boolean = false;

  public constructor(public _alertService: AlertCenterService, private _router: Router, private route: ActivatedRoute, private _fb: FormBuilder
    , private _schedulerService: SchedulerService, ) {

  };

  ngOnInit() {
    this.loading = true;
    this.editForm = this._fb.group({
      jobName: [''],
      groupName: [''],
      year: [''],
      month: [''],
      day: [''],
      hour: [''],
      minute: [''],
      cronExpression: [''],
      cronExpr: ['']
    });
    this.route.queryParams.subscribe(params => {

      var time = Number(params['scheduleTime']);
      var d = Date.parse(time.toString());
      let date = new Date(time);

      this.cronExpression = params['cronExpr'];
      if (this.cronExpression == '') {
        this.cronFlag = true;
      }

      console.log(`CronExpression ${this.cronExpression}`);
      var cronText = `Every ${this.cronExpression.substring(4, 6).trim()} minutes`;



      this.editForm.patchValue({
        jobName: params['jobName'],
        groupName: params['groupName'],
        cronExpr: cronText,
        year: date.getFullYear(),
        month: date.getMonth() + 1,
        day: date.getDate(),
        hour: date.getHours(),
        minute: date.getMinutes()
      });

      this.loading = false;

    });

  }

  updateJob() {
    var jobName = this.editForm.value.jobName;
    var year = this.editForm.value.year;
    var month = this.editForm.value.month;
    var day = this.editForm.value.day;
    var hour = this.editForm.value.hour;
    var minute = this.editForm.value.minute;
    var groupName = this.editForm.value.groupName;

    var data = {
      "jobName": this.editForm.value.jobName,
      "jobScheduleTime": this.getFormattedDate(year, month, day, hour, minute),
      "cronExpression": this.cronExpression,
      "groupName": groupName
    }
    this.loading = true;
    this._schedulerService.updateJob(data).subscribe(
      success => {
        if (success.statusCode == ServerResponseCode.SUCCESS) {
          this.loading = false;
          const alert = new Alert(AlertType.SUCCESS, '', 'Job updated successfully.', 5000, true);
          this._alertService.alert(alert);
          this.cancelEdit();

        } else if (success.statusCode == ServerResponseCode.JOB_DOESNT_EXIST) {
          this.loading = false;
          const alert = new Alert(AlertType.DANGER, '', 'Job no longer exist.', 5000, true);
          this._alertService.alert(alert);
        } else if (success.statusCode == ServerResponseCode.JOB_NAME_NOT_PRESENT) {
          this.loading = false;
          const alert = new Alert(AlertType.DANGER, '', 'Please provide job name.', 5000, true);
          this._alertService.alert(alert);
        }
        this.jobRecords = success.data;
      },
      err => {
        this.loading = false;
        const alert = new Alert(AlertType.DANGER, '', 'Error while updating job.', 5000, true);
        this._alertService.alert(alert);
      });
  }

  editJob(selectedJobRow) {
    this.isEditMode = true;

    var d = Date.parse(selectedJobRow.scheduleTime);
    let date = new Date(selectedJobRow.scheduleTime);
    var cronExpression = selectedJobRow.cronExpr;
    var cronText = `Every ${cronExpression.substring(4, 6).trim()} Minute`;


    this.loading = true;
    this.editForm.patchValue({
      jobName: selectedJobRow.jobName,
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      day: date.getDate(),
      hour: date.getHours(),
      minute: date.getMinutes(),
      groupName: selectedJobRow.groupName,
      cronExpr: cronText
    });

    this.loading = false;
  }

  cancelEdit() {
    this._router.navigate(['./jobs']);

  }

  getFormattedDate(year, month, day, hour, minute) {
    return year + "/" + month + "/" + day + " " + hour + ":" + minute;
  }

  resetForm() {
    var dateNow = new Date();
    this.loading = true;
    this.editForm.patchValue({
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
  cronChange(cronExp) {
    this.editForm.patchValue({
      cronExpression: cronExp
    });
  }
}
