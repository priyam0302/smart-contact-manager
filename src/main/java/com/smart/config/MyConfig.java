package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

//Configuration annotation indicates that the class has @Bean definition methods. So Spring container 
//can process the class and generate Spring Beans to be used in the application.
@Configuration
@EnableWebSecurity
public class MyConfig {

	// Bean annotation is applied on a method to specify that it returns a
	// bean to be managed by Spring context. Spring Bean annotation is usually
	// declared in Configuration classes methods. Spring will create objects for all
	// the methods below and we can directly use them by autowiring.
	@Bean
	UserDetailsService getUserDetailsService() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// DaoAuthenticationProvider is an AuthenticationProvider implementation that
	// uses a UserDetailsService and PasswordEncoder to authenticate a username and
	// password.
	@Bean
	DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(getUserDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

		return daoAuthenticationProvider;
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests.requestMatchers("/admin/**")
				.hasRole("ADMIN").requestMatchers("/user/**").hasRole("USER").requestMatchers("/**").permitAll());
		http.formLogin(login -> login.loginPage("/signin"));
		http.formLogin(login -> login.defaultSuccessUrl("/user/index"));
		http.formLogin(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable);

		return http.build();
	}

}