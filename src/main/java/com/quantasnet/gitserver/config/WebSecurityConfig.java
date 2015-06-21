package com.quantasnet.gitserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		auth
			.inMemoryAuthentication()
				.withUser("user").password("user").roles("USER").and()
				.withUser("admin").password("admin").roles("USER", "ADMIN");
	}
	
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
    public static String channel(final Environment env) {
        return env.acceptsProfiles("openshift") ? "REQUIRES_SECURE_CHANNEL" : "REQUIRES_INSECURE_CHANNEL";
    }
    
	@Configuration
	@Order(1)
	public static class GitRepoSecurity extends WebSecurityConfigurerAdapter {
		
		@Autowired
		private Environment env;
		
		@Override
		protected void configure(final HttpSecurity http) throws Exception {
			http
				.regexMatcher("(\\/repo\\/)(.*)(\\.git)(.*)")
				.authorizeRequests()
					.anyRequest().authenticated()
				.and()
					.httpBasic()
				.and()
					.csrf().disable()
					.headers().disable()
            	.requiresChannel().anyRequest().requires(WebSecurityConfig.channel(env))
	            .and()
	                .sessionManagement().sessionFixation().changeSessionId();
		}
		
	}
	
	@Configuration
	@Order(2)
	public static class WebUISecurity extends WebSecurityConfigurerAdapter {
		
		@Autowired
		private Environment env;
		
		@Override
		public void configure(WebSecurity web) throws Exception {
			web
				.ignoring()
					.antMatchers("/webjars/**", "/js/**");
		}
		
		@Override
		protected void configure(final HttpSecurity http) throws Exception {
			http
	            .authorizeRequests()
	                .antMatchers("/admin/**").hasRole("ADMIN")
	                .anyRequest().authenticated()
	            .and()
	                .formLogin()
	                .loginPage("/login")
	                .permitAll()
	            .and()
	                .logout()
	                .permitAll()
               .and()
	                .requiresChannel().anyRequest().requires(WebSecurityConfig.channel(env))
	            .and()
	                .sessionManagement().sessionFixation().changeSessionId();
	            /*.and()
	                .rememberMe().key(KEY).rememberMeServices(rememberMeServices());*/
		}
		
	}
	
}
