/**
 * 
 */
package org.rta;

import java.util.ArrayList;
import java.util.List;

import org.rta.model.ProcessUser;
import org.rta.service.ActivitiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author admin
 *
 */

@SpringBootApplication
@ComponentScan
public class Application extends SpringBootServletInitializer {
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner init(final ActivitiService service) {

		return new CommandLineRunner() {
			public void run(String... strings) throws Exception {
				ProcessUser user = new ProcessUser();
				user.setUserId("CITIZEN1");
				user.setFirstName("CITIZEN");
				user.setLastName("CITIZEN1");
				user.setPassword("CITIZEN1@123");
				user.setGroupId("ROLE_CITIZEN");
				user.setGroupName("CITIZEN User");
				user.setGroupType("User");
				List<ProcessUser> users = new ArrayList<>();
				users.add(user);
				service.createUsers(users);
			}
		};
	}

}
