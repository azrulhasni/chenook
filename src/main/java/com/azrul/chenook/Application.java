package com.azrul.chenook;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.azrul.chenook.*","com.azrul.smefinancing.*"})
@EnableJpaRepositories(basePackages = {"com.azrul.chenook.repository","com.azrul.smefinancing.repository"},
        repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
@EntityScan({"com.azrul.smefinancing.domain","com.azrul.chenook.domain"})

@EnableJpaAuditing
@EnableEnversRepositories
public class Application  {

	public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
	}
        
    
}
