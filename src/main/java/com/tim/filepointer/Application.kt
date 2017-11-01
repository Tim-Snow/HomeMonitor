package com.tim.filepointer

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan

@ComponentScan
@EnableAutoConfiguration
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}