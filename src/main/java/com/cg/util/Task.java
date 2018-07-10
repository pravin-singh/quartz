package com.cg.util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Table(name="task",schema="nbc_cust")
@Entity
public class Task {
	
	@Id
	@Column(name="taskkey")
	private String taskKey;
	@Column(name="launch_url")
	private String launch_url;
	@Column(name="stop_url")
	private String stop_url;
	@Column(name="env")
	private String envStatus;
	
	public Task() {
		super();
	}
	

	public Task(String taskKey, String launch_url, String stop_url, String envStatus) {
		super();
		this.taskKey = taskKey;
		this.launch_url = launch_url;
		this.stop_url = stop_url;
		this.envStatus = envStatus;
	}

	public String getTaskKey() {
		return taskKey;
	}

	public void setTaskKey(String taskKey) {
		this.taskKey = taskKey;
	}

	public String getLaunch_url() {
		return launch_url;
	}

	public void setLaunch_url(String launch_url) {
		this.launch_url = launch_url;
	}

	public String getStop_url() {
		return stop_url;
	}

	public void setStop_url(String stop_url) {
		this.stop_url = stop_url;
	}

	public String getEnvStatus() {
		return envStatus;
	}

	public void setEnvStatus(String envStatus) {
		this.envStatus = envStatus;
	}


	
	@Override
	public String toString() {
		return "Task [taskKey=" + taskKey + ", launch_url=" + launch_url + ", stop_url=" + stop_url + ", envStatus="
				+ envStatus + "]";
	}

	

	
	
}
