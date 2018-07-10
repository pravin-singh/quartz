package com.cg.job;

import java.util.Date;
import org.apache.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.web.client.RestTemplate;

import com.cg.dao.TaskDao;
import com.cg.service.JobService;
import com.cg.util.ResponseObject;

public class SimpleJob extends QuartzJobBean implements InterruptableJob{
	
	private volatile boolean toStopFlag = true;
	
	public static String START_SERVICE_URL;
	
	@Autowired
	JobService jobService;
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	private Logger logger;
	
	@Autowired
	TaskDao taskDao;

	
	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		JobKey key = jobExecutionContext.getJobDetail().getKey();
	
		logger.info("URl "+START_SERVICE_URL);
		logger.info("Service Call Started for the One Time Schedule");
		ResponseEntity<ResponseObject> responseEntity = restTemplate.getForEntity(START_SERVICE_URL, ResponseObject.class);
		HttpStatus statusCode = responseEntity.getStatusCode();
		logger.info("Service Call Status : "+statusCode);	
		logger.info("Service Call Ended for the One Time Schedule");
		
		jobService.updateEnvStatusForDelete(key.getGroup());
	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		logger.info("Stopping thread... ");
		toStopFlag = false;
	}

}