import { Component, Input, Output, EventEmitter }                              from '@angular/core';
import { Injectable }                                                          from '@angular/core';
import { Http, Response, Headers, RequestOptions, RequestMethod, RequestOptionsArgs, URLSearchParams } from '@angular/http';
import { Subject }                                                             from 'rxjs/Rx';
import { Observable }                                                          from 'rxjs/Rx';
import { URLs } from '../constant/urls.constants';

@Injectable()
export class SchedulerService {

    private options = new RequestOptions(
        {headers: new Headers({'Content-Type': 'application/json'})});

    constructor(
        private _http: Http) {
    }

    getJobs(){
        return this._http.get(URLs.getJobsUrl,this.options)
        .map(resData => resData.json()); 
    }

     getAllTask(){
        return this._http.get(URLs.getTaskUrl)
        .map(resData => resData.json()); 
    }

     checkEnvStatus(data){
      let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
         params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(URLs.checkEnvStatusUrl, this.options)
        .map(resData => resData.json()); 
    }

    updateEnvStatus(data){
      let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(URLs.updateEnvStatusUrl, this.options)
        .map(resData => resData.json()); 
    }

    scheduleJob(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;

        return this._http.get(URLs.scheduleJobUrl, this.options)
        .map(resData => resData.json()); 
    }

    isJobWithNamePresent(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(URLs.isJobWithNamePresentUrl, this.options)
        .map(resData => resData.json()); 
    }

    pauseJob(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(URLs.pauseJobUrl, this.options)
            .map(resData => resData.json()); 
    }

    resumeJob(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(URLs.resumeJobUrl, this.options)
            .map(resData => resData.json()); 
    }

    deleteJob(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(URLs.deleteJobUrl, this.options)
            .map(resData => resData.json()); 
    }
    
    stopJob(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(URLs.stopJobUrl, this.options)
            .map(resData => resData.json()); 
    }

    startJobNow(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(URLs.startJobNowUrl, this.options)
            .map(resData => resData.json()); 
    }

    updateJob(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;

        return this._http.get(URLs.updateJobUrl, this.options)
        .map(resData => resData.json()); 
    }    
}