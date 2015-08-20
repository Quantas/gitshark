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

import com.quantasnet.gitserver.security.GitSharkUserDetailsService;

@Configuration
public class WebSecurityConfig {
	
	static final String GIT_HTTP_REGEX = "(\\/repo\\/)(.*)(?=.*\\.git(?!ignore))(.*)";

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
				.regexMatcher(GIT_HTTP_REGEX)
				.authorizeRequests()
					.anyRequest().authenticated()
				.and()
					.httpBasic()
				.and()
					.csrf().disable()
				.headers()
					.defaultsDisabled()
					.cacheControl().and()
				.and()
				.requiresChannel().anyRequest().requires(WebSecurityConfig.channel(env));
		}
	}
	
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	@Configuration
	@Order(2)
	public static class WebUISecurity extends WebSecurityConfigurerAdapter {
		
		private static final String KEY = "git-shark-remember-me-68debb69-a234-4a4f-8508-ab9234hf8d2a";
		
		@Autowired
		private Environment env;
		
		@Autowired
		private PersistentTokenRepository gitSharkPersistentTokenRepository;
		
		@Autowired
		private PasswordEncoder passwordEncoder;

		@Override
		public void configure(final WebSecurity web) throws Exception {
			web
				.ignoring()
				.antMatchers("/webjars/**", "/js/**", "/css/**");
		}
		
		@Override
		protected void configure(final HttpSecurity http) throws Exception {
			http
				.authorizeRequests()
					.antMatchers("/profile/**").authenticated()
					.antMatchers("/admin/**").hasRole("ADMIN")
					.antMatchers("/management/**").hasRole("ADMIN")
					.antMatchers("/404", "/403", "/401", "/503").permitAll()
					.antMatchers("/register").anonymous()
					.anyRequest().authenticated()
				.and()
					.formLogin()
						.loginPage("/login")
						.permitAll()
				.and()
					.logout()
						.permitAll()
				.and()
					.csrf()
				.and()
					.headers()
				.and()
					.requiresChannel().anyRequest().requires(WebSecurityConfig.channel(env))
				.and()
					.rememberMe().key(KEY).rememberMeServices(rememberMeServices());
		}
		
		@Autowired
		public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
			auth
				.userDetailsService(gitSharkUserDetailsService())
				.passwordEncoder(passwordEncoder);
		}

		@Bean
		public UserDetailsService gitSharkUserDetailsService() {
			return new GitSharkUserDetailsService();
		}
		
		@Bean
		@Override
		public AuthenticationManager authenticationManagerBean() throws Exception {
			return super.authenticationManagerBean();
		}

		@Bean
		public RememberMeServices rememberMeServices() {
			final PersistentTokenBasedRememberMeServices rememberMeServices =
				new PersistentTokenBasedRememberMeServices(KEY, gitSharkUserDetailsService(), gitSharkPersistentTokenRepository);
			rememberMeServices.setCookieName("GIT_SHARK_REMEMBER_ME");
			rememberMeServices.setParameter("_git_shark_remember_me");
			return rememberMeServices;
		}
	}
}
