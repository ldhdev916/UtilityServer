package com.ldhdev.utilityserver.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {

        http
            .cors().and()
            .csrf().ignoringAntMatchers("/android/execution", "/selfTest", "/backup/**", "/benedu")
        http
            .authorizeRequests()
            .antMatchers("/nameless/admin/**").authenticated()

        http
            .formLogin()
            .permitAll()
    }
}