package com.tim.filepointer

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer

@SpringBootApplication
@EnableResourceServer
@EnableAutoConfiguration
open class Application : WebSecurityConfigurerAdapter()

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
