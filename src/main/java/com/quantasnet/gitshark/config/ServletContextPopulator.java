package com.quantasnet.gitshark.config;

import java.net.InetAddress;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServletContextPopulator {

	@Autowired
	private ServletContext servletContext;

	@PostConstruct
	void postConstruct() {
		servletContext.setAttribute("serverName", serverName());
	}

	private String serverName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch(final Exception e) {
			return "unknown";
		}
	}

}
