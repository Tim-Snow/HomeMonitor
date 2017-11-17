package com.tim.filepointer

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.oauth2.client.OAuth2ClientContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.config.annotation.web.builders.HttpSecurity


@ComponentScan
@EnableOAuth2Client
@EnableAutoConfiguration
class Application : WebSecurityConfigurerAdapter()

@Autowired var oauth2ClientContext: OAuth2ClientContext? = null


fun main(args: Array<String>)  {
    SpringApplication.run(Application::class.java, *args)
}

@Override fun configure(http: HttpSecurity) {

}