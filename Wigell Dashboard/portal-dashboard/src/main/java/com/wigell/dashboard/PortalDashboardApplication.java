package com.wigell.dashboard;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
public class PortalDashboardApplication {
	public static void main(String[] args) {
		SpringApplication.run(PortalDashboardApplication.class, args);
	}
}