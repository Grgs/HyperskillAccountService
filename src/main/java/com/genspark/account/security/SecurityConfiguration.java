package com.genspark.account.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    AuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    AccessDeniedHandler accessDeniedHandler;
    @Autowired
    AuthenticationFailureHandler authenticationFailureHandler;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeRequests()
//                .anyRequest().permitAll()
                .mvcMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .mvcMatchers(HttpMethod.POST, "/api/auth/changepass").hasAnyRole(
                        ROLE.USER.toString(), ROLE.ACCOUNTANT.toString(), ROLE.ADMINISTRATOR.toString())
                .mvcMatchers(HttpMethod.GET, "/api/empl/payment").hasAnyRole(
                        ROLE.USER.toString(), ROLE.ACCOUNTANT.toString())
                .mvcMatchers(HttpMethod.POST, "/api/acct/payments").hasRole(ROLE.ACCOUNTANT.toString())
                .mvcMatchers(HttpMethod.PUT, "/api/acct/payments").hasRole(ROLE.ACCOUNTANT.toString())
                .mvcMatchers(HttpMethod.GET, "/api/admin/user").hasRole(ROLE.ADMINISTRATOR.toString())
                .mvcMatchers(HttpMethod.DELETE, "/api/admin/user/**").hasRole(ROLE.ADMINISTRATOR.toString())
                .mvcMatchers(HttpMethod.PUT, "/api/admin/user/role/**").hasRole(ROLE.ADMINISTRATOR.toString())
                .mvcMatchers(HttpMethod.GET, "/api/security/events").hasRole(ROLE.AUDITOR.toString())
                .mvcMatchers("/actuator/shutdown", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
                .and().httpBasic().authenticationEntryPoint(authenticationEntryPoint)
                .and().exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .and().csrf().disable().headers().frameOptions().disable()
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(getPasswordEncoder());
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder(13);
    }

}
