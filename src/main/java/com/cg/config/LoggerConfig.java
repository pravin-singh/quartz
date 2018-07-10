package com.cg.config;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class LoggerConfig {

	@Bean
	@Scope("prototype")
	public Logger logger(InjectionPoint injectionPoint) {
		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass());
		
	}
}
