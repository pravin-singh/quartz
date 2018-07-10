package com.cg.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.quartz.QuartzJobBean;

import com.cg.job.CronJob;
import com.cg.util.Task;

public interface JobService {
	boolean scheduleOneTimeJob(String jobName, Class job, Date date,String taskKey);
	boolean scheduleCronJob(String jobName, Class job, Date date, String cronExpression,String taskKey);
	
	boolean updateOneTimeJob(String jobName, Date date);
	boolean updateCronJob(String jobName, Date date, String cronExpression);
	
	boolean unScheduleJob(String jobName);
	boolean deleteJob(String jobName,String groupName);
	boolean pauseJob(String jobName,String groupName);
	boolean resumeJob(String jobName,String groupName);
	boolean startJobNow(String jobName,String groupName);
	boolean isJobRunning(String jobName,String groupName);
	List<Map<String, Object>> getAllJobs();
	boolean isJobWithNamePresent(String jobName,String groupName);
	String getJobState(String jobName,String groupName);
	boolean stopJob(String jobName,String groupName);
	
	
	List<Task> getAllTask();
	
	boolean updateEnvStatusForSchedule(String taskKey);
	boolean updateEnvStatusForDelete(String taskKey);
	boolean checkEnvStatus(String taskKey);
	
	
	
}
