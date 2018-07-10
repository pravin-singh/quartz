package com.cg.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.cg.dto.ServerResponse;
import com.cg.job.CronJob;
import com.cg.job.SimpleJob;
import com.cg.service.JobService;
import com.cg.util.ServerResponseCode;
import com.cg.util.Task;


@RestController
@RequestMapping("/scheduler/")
public class JobController {

	@Autowired
	private  Logger logger;
	@Autowired
	@Lazy
	JobService jobService;
	
	
	/**
	 * @param jobName
	 * @param jobScheduleTime
	 * @param cronExpression
	 * @return ServerResponse
	 * For scheduling the job with unique name as a Single trigger / cron trigger
	 */
	@RequestMapping("schedule")	
	public ServerResponse schedule(@RequestParam("jobName") String jobName, 
			@RequestParam("jobScheduleTime") @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm") Date jobScheduleTime, 
			@RequestParam("cronExpression") String cronExpression,@RequestParam("startTask") String startTask,@RequestParam("taskName") String taskName){

		//Job Name is mandatory
		if(jobName == null || jobName.trim().equals("")){
			logger.info("Job name is mandatory : plese provide jobName ");
			return getServerResponse(ServerResponseCode.JOB_NAME_NOT_PRESENT, false);
		}
		
		//Job Name is mandatory
		if(startTask == null || startTask.trim().equals("")){
			logger.info("Job task is mandatory : plese provide jobName ");
			return getServerResponse(ServerResponseCode.JOB_TASK_NOT_PRESENT, false);
		}
		
		
		boolean checkTaskflag = false;
		checkTaskflag = jobService.checkEnvStatus(taskName);
		 
		 if(checkTaskflag==true) {
			 return getServerResponse(ServerResponseCode.TASK_IS_ALREADY_SHEDULE, checkTaskflag);
		 }
		
		
		logger.info("Scheduling Job with name : "+jobName);

		//Check if job Name is unique;
		if(!jobService.isJobWithNamePresent(jobName,taskName)){

			if(cronExpression == null || cronExpression.trim().equals("")){
				SimpleJob.START_SERVICE_URL = startTask;
				//Single Trigger
				boolean status = jobService.scheduleOneTimeJob(jobName, SimpleJob.class, jobScheduleTime,taskName);
				if(status){
					logger.info("Job Scheduled with name : "+jobName+" as Single triggered");
					 boolean flag = jobService.updateEnvStatusForSchedule(taskName);
						if(flag) {
							return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAllJobs());
						}
						
				}else{
					logger.info("Job not Scheduled with name : "+jobName+" as Single triggered becoze of some internal problem");
					return getServerResponse(ServerResponseCode.ERROR, false);
				}
				
			}else{
				//Cron Trigger
				logger.info("Task To shedule : "+startTask);
				CronJob.START_SERVICE_URL = startTask;
				
				boolean status = jobService.scheduleCronJob(jobName, CronJob.class, jobScheduleTime, cronExpression,taskName);
				if(status){
					logger.info("Job Scheduled with name : "+jobName+" as cron triggered");
					
					 boolean flag = jobService.updateEnvStatusForSchedule(taskName);
					if(flag) {
						return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAllJobs());
					}
				}else{
					logger.info("Job not Scheduled with name : "+jobName+" as cron triggered becoze of some internal problem");
					return getServerResponse(ServerResponseCode.ERROR, false);
				}				
			}
		}else{
			logger.info("Job already exist with name : "+jobName);
			return getServerResponse(ServerResponseCode.JOB_WITH_SAME_NAME_EXIST, false);
		}
		return null;
	}

	/**
	 * @param jobName
	 * For UnScheduling the job using job name
	 */
	@RequestMapping("unschedule")
	public void unschedule(@RequestParam("jobName") String jobName) {
		logger.info(" UnScheduling the job using job name : "+jobName+"  from schedule list");
		jobService.unScheduleJob(jobName);
		logger.info(" UnScheduled job using job name : "+jobName+"  from schedule list");
	}

	/**
	 * @param jobName
	 * @return ServerResponse
	 * For deleting the job using job name from the job list
	 */
	@RequestMapping("delete")
	public ServerResponse delete(@RequestParam("jobName") String jobName,@RequestParam("groupName") String groupName) {
		logger.info(" deleting the job using job name : "+jobName+"  from job list");

		if(jobService.isJobWithNamePresent(jobName,groupName)){
			boolean isJobRunning = false;
			isJobRunning = jobService.isJobRunning(jobName,groupName);

			if(!isJobRunning){
				boolean status = jobService.deleteJob(jobName,groupName);
				if(status){
					logger.info(" job is deleted using job name : "+jobName+"  from job list");
					jobService.updateEnvStatusForDelete(groupName);
					
					return getServerResponse(ServerResponseCode.SUCCESS, true);
				}else{
					logger.info("job is not deleted using job name : "+jobName+"  from job list");
					return getServerResponse(ServerResponseCode.ERROR, false);
				}
			}else{
				return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false);
			}
		}else{
			//Job doesn't exist
			logger.info(" job view does not exist with the name of : [ "+jobName+" ] in job list");
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}

	/**
	 * @param jobName
	 * @return ServerResponse
	 * For pause the job using job name
	 */
	@RequestMapping("pause")
	public ServerResponse pause(@RequestParam("jobName") String jobName,@RequestParam("groupName") String groupName) {
		logger.info(" Pausing the job using job name : "+jobName+"");

		if(jobService.isJobWithNamePresent(jobName,groupName)){

			boolean isJobRunning = jobService.isJobRunning(jobName,groupName);

			if(!isJobRunning){
				boolean status = jobService.pauseJob(jobName,groupName);
				if(status){
					logger.info("job paused using job name : "+jobName+" with status "+status);
					return getServerResponse(ServerResponseCode.SUCCESS, true);
				}else{
					logger.info("job not paused using job name : "+jobName+" with status "+status);
					return getServerResponse(ServerResponseCode.ERROR, false);
				}			
			}else{				
				return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false);
			}

		}else{
			//Job doesn't exist
			logger.info("job view does not exist with  job name :[ "+jobName+" ]");
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}		
	}

	/**
	 * @param jobName
	 * @return ServerResponse
	 * For Resuming the job using Job Name
	 */
	@RequestMapping("resume")
	public ServerResponse resume(@RequestParam("jobName") String jobName,@RequestParam("groupName") String groupName) {
		logger.info(" job is resuming using job name : "+jobName);

		if(jobService.isJobWithNamePresent(jobName,groupName)){
			String jobState = jobService.getJobState(jobName,groupName);

			if(jobState.equals("PAUSED")){
				
				boolean status = jobService.resumeJob(jobName,groupName);

				if(status){
					logger.info(" job is resumed ");
					return getServerResponse(ServerResponseCode.SUCCESS, true);
				}else{
					logger.info(" job is not resumed ");
					return getServerResponse(ServerResponseCode.ERROR, false);
				}
			}else{
				return getServerResponse(ServerResponseCode.JOB_NOT_IN_PAUSED_STATE, false);
			}

		}else{
			logger.info(" job does not exist with name : "+jobName);
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}

	/**
	 * @param jobName
	 * @param jobScheduleTime
	 * @param cronExpression
	 * @return ServerResponse
	 * For updating the job according to the job name
	 */
	@RequestMapping("update")
	public ServerResponse updateJob(@RequestParam("jobName") String jobName, 
			@RequestParam("jobScheduleTime") @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm") Date jobScheduleTime, 
			@RequestParam("cronExpression") String cronExpression,
			@RequestParam("groupName") String groupName){
		logger.info(" job is updating using job name : "+jobName);

		//Job Name is mandatory
		if(jobName == null || jobName.trim().equals("")){
			logger.info(" job name is mandatory please provide job name");
			return getServerResponse(ServerResponseCode.JOB_NAME_NOT_PRESENT, false);
		}

		//Edit Job
		if(jobService.isJobWithNamePresent(jobName,groupName)){
			
			if(cronExpression == null || cronExpression.trim().equals("")){
				boolean status = jobService.updateOneTimeJob(jobName, jobScheduleTime);
				if(status){
					logger.info(" job is updated using job name : "+jobName+" as single triggered job");
					return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAllJobs());
				}else{
					logger.info(" job is not updated using job name : "+jobName+" as single triggered job");
					return getServerResponse(ServerResponseCode.ERROR, false);
				}
				
			}else{
				//Cron Trigger
				boolean status = jobService.updateCronJob(jobName, jobScheduleTime, cronExpression);
				if(status){
					logger.info(" job is updated using job name : "+jobName+" as cron triggerd job");
					return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAllJobs());
				}else{
					logger.info(" job is not updated using job name : "+jobName+" as cron triggerd job");
					return getServerResponse(ServerResponseCode.ERROR, false);
				}				
			}
			
			
		}else{
			logger.info(" job does not exist with job name : "+jobName);
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}

	/**
	 * @return ServerResponse
	 * For get all the jobs from the job list
	 */
	@RequestMapping("jobs")
	public ServerResponse getAllJobs(){
		logger.info("Getting all the jobs from job list");

		List<Map<String, Object>> list = jobService.getAllJobs();
		logger.info("Got all the jobs from job list");
		return getServerResponse(ServerResponseCode.SUCCESS, list);
	}

	/**
	 * @param jobName
	 * @return ServerResponse
	 * For checking job name is available or not in the job list
	 */
	@RequestMapping("checkJobName")
	public ServerResponse checkJobName(@RequestParam("jobName") String jobName,@RequestParam("groupName") String groupName){
		logger.info("checking job name");

		//Job Name is mandatory
		if(jobName == null || jobName.trim().equals("")){
			logger.info("Job name is not available in the list or jobName==null ");
			return getServerResponse(ServerResponseCode.JOB_NAME_NOT_PRESENT, false);
		}
		
		boolean status = jobService.isJobWithNamePresent(jobName,groupName);
		logger.info("job name [ "+jobName+" ] is available with status "+status);
		return getServerResponse(ServerResponseCode.SUCCESS, status);
	}

	/**
	 * @param jobName
	 * @return ServerResponse
	 * For checking job is in running state or not
	 */
	@RequestMapping("isJobRunning")
	public ServerResponse isJobRunning(@RequestParam("jobName") String jobName,@RequestParam("groupName") String groupName) {
		boolean status = jobService.isJobRunning(jobName,groupName);
		if(status){
			logger.info("job is in running state with the status of "+status);
			return getServerResponse(ServerResponseCode.SUCCESS, status);
		}
		else {
			logger.info("job is not in running state with the status of "+status);
			return getServerResponse(ServerResponseCode.ERROR, status);
		}
	}

	/**
	 * @param jobName
	 * @return ServerResponse
	 * For checking the job state like ( SCHEDULED,PAUSED,BLOCKED,COMPLETE,ERROR or NONE ) using job name
	 */
	@RequestMapping("jobState")
	public ServerResponse getJobState(@RequestParam("jobName") String jobName,@RequestParam("groupName") String groupName) {
		String jobState = jobService.getJobState(jobName,groupName);
		if(jobState!=null && 
				(jobState.equalsIgnoreCase("SCHEDULED") || jobState.equalsIgnoreCase("PAUSED") || jobState.equalsIgnoreCase("BLOCKED")
						|| jobState.equalsIgnoreCase("COMPLETE") || jobState.equalsIgnoreCase("ERROR") || jobState.equalsIgnoreCase("NONE"))) {
			logger.info("job state is ::"+jobState);
			return getServerResponse(ServerResponseCode.SUCCESS, jobState);
		}
		else {
			logger.info("job is not exist ::"+jobState);
			return getServerResponse(ServerResponseCode.ERROR, jobState);

		}
	}

	/**
	 * @param jobName
	 * @return ServerResponse
	 * For stopping the running job using jobName
	 */
	@RequestMapping("stop")
	public ServerResponse stopJob(@RequestParam("jobName") String jobName,@RequestParam("groupName") String groupName) {
		logger.info("stopping the job using job name : "+jobName);

		if(jobService.isJobWithNamePresent(jobName,groupName)){

			/*if(jobService.isJobRunning(jobName,groupName)){
				boolean status = jobService.stopJob(jobName,groupName);
				if(status){
					logger.info("job is stopped using job name : "+jobName);
						
					return getServerResponse(ServerResponseCode.SUCCESS, true);
				}else{
					//Server error
					logger.info("unable to stop job using job name : "+jobName);
					return getServerResponse(ServerResponseCode.ERROR, false);
				}

			}else{
				//Job not in running state
				logger.info("currently job is not running or it is in stop state ");
				return getServerResponse(ServerResponseCode.JOB_NOT_IN_RUNNING_STATE, false);
			}*/
			
			boolean status = jobService.stopJob(jobName,groupName);
			if(status){
				logger.info("job is stopped using job name : "+jobName);
					
				return getServerResponse(ServerResponseCode.SUCCESS, true);

		}else{
			//Job doesn't exist
			logger.info("job does not exist with job name : "+jobName);
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}
		return null;
	}

	/**
	 * @param jobName
	 * @return ServerResponse
	 * For starting the job using job name
	 */
	@RequestMapping("start")
	public ServerResponse startJobNow(@RequestParam("jobName") String jobName,@RequestParam("groupName") String groupName) {
		logger.info("starting the job using job name : "+jobName);
		
		if(jobService.isJobWithNamePresent(jobName,groupName)){

			if(!jobService.isJobRunning(jobName,groupName)){
				boolean status = jobService.startJobNow(jobName,groupName);

				if(status){
					//Success
					logger.info("job started successfully using job name : "+jobName);
					return getServerResponse(ServerResponseCode.SUCCESS, true);

				}else{
					//Server error
					logger.info("unable to start the job using job name : "+jobName);
					return getServerResponse(ServerResponseCode.ERROR, false);
				}

			}else{
				//Job already running
				logger.info("job already in running state and job name is : "+jobName);
				return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false);
			}

		}else{
			//Job doesn't exist
			logger.info("job does not exist with job name : "+jobName);
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}
	
	@RequestMapping("/tasks")
	public ServerResponse getAllTask() {
		
		List<Task> taskList = jobService.getAllTask();
		logger.info("Tasks List "+taskList.toString());
		
		return getServerResponse(ServerResponseCode.SUCCESS, taskList);
		
	}
	
	
	/**
	 * @param responseCode
	 * @param data
	 * @return ServerResponse
	 * For getting server response using Job data and it's response code
	 */
	public ServerResponse getServerResponse(int responseCode, Object data){
		ServerResponse serverResponse = new ServerResponse();
		serverResponse.setStatusCode(responseCode);
		serverResponse.setData(data);
		return serverResponse; 
	}
}
