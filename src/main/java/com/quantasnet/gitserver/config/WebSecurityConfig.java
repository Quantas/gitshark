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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

import com.quantasnet.gitserver.security.GitServerUserDetailsService;

@Configuration
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
    private static String channel(final Environment env) {
        return env.acceptsProfiles("openshift") ? "REQUIRES_SECURE_CHANNEL" : "REQUIRES_INSECURE_CHANNEL";
    }
    
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
	@Configuration
	@Order(1)
	public static class GitRepoSecurity extends WebSecurityConfigurerAdapter {
		
		@Autowired
		private Environment env;
		
		@Override
		protected void configure(final HttpSecurity http) throws Exception {
			http
				.regexMatcher("(\\/repo\\/)(.*)(?=.*\\.git(?!ignore))(.*)")
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
	
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
	@Configuration
	@Order(2)
	public static class WebUISecurity extends WebSecurityConfigurerAdapter {
		
		private static final String KEY = "git-server-remember-me-68debb69-a234-4a4f-8508-ab9234hf8d2a";
		
		@Autowired
		private Environment env;
		
	    @Autowired
	    private PersistentTokenRepository gitServerPersistentTokenRepository;
		
		@Autowired
		private PasswordEncoder passwordEncoder;
	    
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
	                .antMatchers("/management/**").hasRole("ADMIN")
	                .antMatchers("/404", "/403", "/401", "/503").permitAll()
	                .anyRequest().authenticated()
	            .and()
	                .formLogin()
	                .loginPage("/login")
	                .permitAll()
	            .and()
	                .logout()
	                .permitAll()
               .and()
               .csrf().disable()
	                .requiresChannel().anyRequest().requires(WebSecurityConfig.channel(env))
	            .and()
	                .sessionManagement().sessionFixation().changeSessionId()
	            .and()
	                .rememberMe().key(KEY).rememberMeServices(rememberMeServices());
		}
		
		@Autowired
	    public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
	        auth
	            .userDetailsService(gitServerUserDetailsService())
	            .passwordEncoder(passwordEncoder);
	    }
	    
		@Bean
		public UserDetailsService gitServerUserDetailsService() {
			return new GitServerUserDetailsService();
		}
		
	    @Bean
	    @Override
	    public AuthenticationManager authenticationManagerBean() throws Exception {
	        return super.authenticationManagerBean();
	    }
		
	    @Bean
	    public RememberMeServices rememberMeServices() {
	        final PersistentTokenBasedRememberMeServices rememberMeServices =
	                new PersistentTokenBasedRememberMeServices(KEY, userDetailsService(), gitServerPersistentTokenRepository);
	        rememberMeServices.setCookieName("GIT_SERVER_REMEMBER_ME");
	        rememberMeServices.setParameter("_git_server_remember_me");
	        return rememberMeServices;
	    }
	    
	    @Bean
	    public RememberMeAuthenticationFilter rememberMeAuthenticationFilter() throws Exception {
	        return new RememberMeAuthenticationFilter(authenticationManager(), rememberMeServices());
	    }
	}
}
