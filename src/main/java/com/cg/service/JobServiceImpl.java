package com.cg.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cg.dao.TaskDao;
import com.cg.job.CronJob;
import com.cg.util.ResponseObject;
import com.cg.util.Task;


@Service
public class JobServiceImpl implements JobService{

	@Autowired
	private Logger logger;
	
	@Autowired
	@Lazy
	SchedulerFactoryBean schedulerFactoryBean;

	@Autowired
	private ApplicationContext context;
	
	@Autowired
	RestTemplate restTemplate;
	
	@Value("${service.stop.url}")
	public String STOP_SERVICE_URL;
		
	@Autowired
	TaskDao taskDao;

	/**
	 * Schedule a job by jobName at given date.
	 */
	@Override
	public boolean scheduleOneTimeJob(String jobName, Class jobClass, Date date,String taskKey) {
		logger.info("Request received to scheduleJob");

		String jobKey = jobName;
		String groupKey = taskKey;	
		String triggerKey = jobName;		

		JobDetail jobDetail = JobUtil.createJob(jobClass, false, context, jobKey, groupKey);

		logger.info("creating trigger for key :"+jobKey + " at date :"+date);
		 
		Trigger cronTriggerBean = JobUtil.createSingleTrigger(triggerKey, date, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

		try {
			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			Date dt = scheduler.scheduleJob(jobDetail, cronTriggerBean);
			logger.info("Job with key jobKey :"+jobKey+ " and group :"+groupKey+ " scheduled successfully for date :"+dt);
			return true;
		} catch (SchedulerException e) {
			logger.info("SchedulerException while scheduling job with key :"+jobKey + " message :"+e.getMessage());
			e.printStackTrace();
		}

		return false;
	}
	
	/**
	 * Schedule a job by jobName at given date.
	 */
	@Override
	public boolean scheduleCronJob(String jobName, Class jobClass, Date date, String cronExpression,String taskKey) {

		String jobKey = jobName;
		String groupKey = taskKey;	
		String triggerKey = jobName;		
		
		JobDetail jobDetail = JobUtil.createJob(jobClass, false, context, jobKey, groupKey);
		Trigger cronTriggerBean = JobUtil.createCronTrigger(triggerKey, date, cronExpression, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

		try {
			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			Date dt = scheduler.scheduleJob(jobDetail, cronTriggerBean);
			logger.info("Job with key jobKey :"+jobKey+ " and group :"+groupKey+ " scheduled successfully for date :"+dt);
			return true;
		} catch (SchedulerException e) {
			logger.info("SchedulerException while scheduling job with key :"+jobKey + " message :"+e.getMessage());
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Update one time scheduled job.
	 */
	@Override
	public boolean updateOneTimeJob(String jobName, Date date) {

		String jobKey = jobName;
		try {
			Trigger newTrigger = JobUtil.createSingleTrigger(jobKey, date, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

			Date dt = schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobKey), newTrigger);
			logger.info("Trigger associated with jobKey :"+jobKey+ " rescheduled successfully for date :"+dt);
			return true;
		} catch ( Exception e ) {
			logger.info("SchedulerException while updating one time job with key :"+jobKey + " message :"+e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Update scheduled cron job.
	 */
	@Override
	public boolean updateCronJob(String jobName, Date date, String cronExpression) {

		String jobKey = jobName;

		try {
			Trigger newTrigger = JobUtil.createCronTrigger(jobKey, date, cronExpression, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

			Date dt = schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobKey), newTrigger);
			logger.info("Trigger associated with jobKey :"+jobKey+ " rescheduled successfully for date :"+dt);
			return true;
		} catch ( Exception e ) {
			logger.info("SchedulerException while updating cron job with key :"+jobKey + " message :"+e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Remove the indicated Trigger from the scheduler. 
	 * If the related job does not have any other triggers, and the job is not durable, then the job will also be deleted.
	 */
	@Override
	public boolean unScheduleJob(String jobName) {

		String jobKey = jobName;

		TriggerKey tkey = new TriggerKey(jobKey);
		try {
			boolean status = schedulerFactoryBean.getScheduler().unscheduleJob(tkey);
			logger.info("Trigger associated with jobKey :"+jobKey+ " unscheduled with status :"+status);
			return status;
		} catch (SchedulerException e) {
			logger.info("SchedulerException while unscheduling job with key :"+jobKey + " message :"+e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Delete the identified Job from the Scheduler - and any associated Triggers.
	 */
	@Override
	public boolean deleteJob(String jobName,String groupName) {

		String jobKey = jobName;
		String groupKey = groupName;

		JobKey jkey = new JobKey(jobKey, groupKey); 
		try {
			boolean status = schedulerFactoryBean.getScheduler().deleteJob(jkey);
			logger.info("Job with jobKey :"+jobKey+ " deleted with status :"+status);
			return status;
		} catch (SchedulerException e) {
			logger.info("SchedulerException while deleting job with key :"+jobKey + " message :"+e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Pause a job
	 */
	@Override
	public boolean pauseJob(String jobName,String groupName) {
		String jobKey = jobName;
		String groupKey = groupName;
		JobKey jkey = new JobKey(jobKey, groupKey); 

		try {
			schedulerFactoryBean.getScheduler().pauseJob(jkey);
			logger.info("Job with jobKey :"+jobKey+ " paused succesfully.");
			logger.info("Service Stop Call Started");
			
			Task task = taskDao.getOne(groupName);
			String stop_url = task.getStop_url();
			
			ResponseEntity<Object[]> responseEntity = restTemplate.getForEntity(stop_url, Object[].class);
			HttpStatus statusCode = responseEntity.getStatusCode();
			
			logger.info("Service Stop Call Status : "+statusCode);	
			logger.info("Service Stop Call Ended");
			return true;
		} catch (SchedulerException e) {
            logger.info("SchedulerException while pausing job with key :"+jobName + " message :"+e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Resume paused job
	 */
	@Override
	public boolean resumeJob(String jobName,String groupName) {

		String jobKey = jobName;
		String groupKey = groupName;
		JobKey jKey = new JobKey(jobKey, groupKey); 
		try {
			schedulerFactoryBean.getScheduler().resumeJob(jKey);
			logger.info("Job with jobKey :"+jobKey+ " resumed succesfully.");
			
			Task task = taskDao.getOne(groupName);
			String start_url = task.getLaunch_url();
			
			logger.info("URl "+start_url);
			logger.info("Service Call Started");
			ResponseEntity<ResponseObject> responseEntity = restTemplate.getForEntity(start_url, ResponseObject.class);
			HttpStatus statusCode = responseEntity.getStatusCode();
			logger.info("Service Call Status : "+statusCode);	
			logger.info("Service Call Ended");
			
			
			return true;
		} catch (SchedulerException e) {
			logger.info("SchedulerException while resuming job with key :"+jobKey+ " message :"+e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Start a job now
	 */
	@Override
	public boolean startJobNow(String jobName,String groupName) {
		String jobKey = jobName;
		String groupKey = groupName;

		JobKey jKey = new JobKey(jobKey, groupKey); 
		try {
			schedulerFactoryBean.getScheduler().triggerJob(jKey);
			logger.info("Job with jobKey :"+jobKey+ " started now succesfully.");
			
			Task task = taskDao.getOne(groupName);
			String start_url = task.getLaunch_url();
			
			logger.info("URl "+start_url);
			logger.info("Service Call Started");
			ResponseEntity<ResponseObject> responseEntity = restTemplate.getForEntity(start_url, ResponseObject.class);
			HttpStatus statusCode = responseEntity.getStatusCode();
			logger.info("Service Call Status : "+statusCode);	
			logger.info("Service Call Ended");
			
			
			return true;
		} catch (SchedulerException e) {
			logger.info("SchedulerException while starting job now with key :"+jobKey+ " message :"+e.getMessage());
			e.printStackTrace();
			return false;
		}		
	}

	/**
	 * Check if job is already running or not
	 */
	@Override
	public boolean isJobRunning(String jobName,String groupName) {
		logger.info("Request received to check if job is running");

		String jobKey = jobName;
		String groupKey = groupName;

		logger.info("Parameters received for checking job is running now : jobKey :"+jobKey);
		try {

			List<JobExecutionContext> currentJobs = schedulerFactoryBean.getScheduler().getCurrentlyExecutingJobs();
			if(currentJobs!=null){
				for (JobExecutionContext jobCtx : currentJobs) {
					String jobNameDB = jobCtx.getJobDetail().getKey().getName();
					String groupNameDB = jobCtx.getJobDetail().getKey().getGroup();
					if (jobKey.equalsIgnoreCase(jobNameDB) && groupKey.equalsIgnoreCase(groupNameDB)) {
						return true;
					}
				}
			}
		} catch (SchedulerException e) {
			logger.info("SchedulerException while checking job with key :"+jobKey+ " is running. error message :"+e.getMessage());
			e.printStackTrace();
			return false;
		}
		return false;
	}

	/**
	 * Get all jobs
	 */
	@Override
	public List<Map<String, Object>> getAllJobs() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			 String cronExpr = "";
			

			for (String groupName : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

					String jobName = jobKey.getName();
					String jobGroup = jobKey.getGroup();

					//get job's trigger
					List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
					Date scheduleTime = triggers.get(0).getStartTime();
					Date nextFireTime = triggers.get(0).getNextFireTime();
					Date lastFiredTime = triggers.get(0).getPreviousFireTime();
					
					for (Trigger trigger : triggers) {
					    if (trigger instanceof CronTrigger) {
					        CronTrigger cronTrigger = (CronTrigger) trigger;
					        cronExpr  = cronTrigger.getCronExpression();
					    }
					}
					
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("jobName", jobName);
					map.put("groupName", jobGroup);
					map.put("scheduleTime", scheduleTime);
					map.put("lastFiredTime", lastFiredTime);
					map.put("nextFireTime", nextFireTime);
					map.put("cronExpr", cronExpr);
					
					if(isJobRunning(jobName,jobGroup)){
						map.put("jobStatus", "RUNNING");
					}else{
						String jobState = getJobState(jobName,jobGroup);
						map.put("jobStatus", jobState);
					}

					list.add(map);
					logger.info("Job details:");
					logger.info("Job Name: "+jobName + ", Group Name: "+ groupName + ", Schedule Time: "+scheduleTime);
				}

			}
		} catch (SchedulerException e) {
			logger.info("SchedulerException while fetching all jobs. error message :"+e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Check job exist with given name or not
	 */
	@Override
	public boolean isJobWithNamePresent(String jobName,String groupName) {
		try {
			String groupKey = groupName;
			JobKey jobKey = new JobKey(jobName, groupKey);
			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			if (scheduler.checkExists(jobKey)){
				return true;
			}
		} catch (SchedulerException e) {
			logger.info("SchedulerException while checking job with name and group exist:"+e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Get the current state of job
	 */
	public String getJobState(String jobName,String groupName) {
		try {
			String groupKey = groupName;
			JobKey jobKey = new JobKey(jobName, groupKey);

			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);

			List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
			if(triggers != null && triggers.size() > 0){
				for (Trigger trigger : triggers) {
					TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());

					if (TriggerState.PAUSED.equals(triggerState)) {
						return "PAUSED";
					}else if (TriggerState.BLOCKED.equals(triggerState)) {
						return "BLOCKED";
					}else if (TriggerState.COMPLETE.equals(triggerState)) {
						return "COMPLETE";
					}else if (TriggerState.ERROR.equals(triggerState)) {
						return "ERROR";
					}else if (TriggerState.NONE.equals(triggerState)) {
						return "NONE";
					}else if (TriggerState.NORMAL.equals(triggerState)) {
						return "SCHEDULED";
					}
				}
			}
		} catch (SchedulerException e) {
			logger.info("SchedulerException while checking job with name and group exist:"+e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Stop a job
	 */
	@Override
	public boolean stopJob(String jobName,String groupName) {
		try{	
			String jobKey = jobName;
			String groupKey = groupName;

			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			JobKey jkey = new JobKey(jobKey, groupKey);
			
			return scheduler.interrupt(jkey);

		} catch (SchedulerException e) {
			logger.info("SchedulerException while stopping job. error message :"+e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public List<Task> getAllTask() {
		List<Task> taskList =  taskDao.findAll();
		return taskList;
	}

	@Override
	public boolean updateEnvStatusForSchedule(String taskKey) {
		Task task = taskDao.findOne(taskKey);
		task.setEnvStatus("true");
		taskDao.save(task);
		
		if(task.getEnvStatus().equalsIgnoreCase("true")) {
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	public boolean updateEnvStatusForDelete(String taskKey) {
		Task task = taskDao.findOne(taskKey);
		task.setEnvStatus("false");
		taskDao.save(task);
		
		if(task.getEnvStatus().equalsIgnoreCase("true")) {
			return true;
		}else {
			return false;
		}
	}

	@Override
	public boolean checkEnvStatus(String taskKey) {
		Task task = taskDao.findOne(taskKey);
		if(task.getEnvStatus().equalsIgnoreCase("true")) {
			return true;
		}else {
			return false;
		}
		
	}
}

