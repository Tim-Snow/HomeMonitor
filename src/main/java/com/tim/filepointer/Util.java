package com.tim.filepointer;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class Util {

    public static void initiateShutdown(ApplicationContext context, int returnCode){
        SpringApplication.exit(context, () -> returnCode);
    }

    public static boolean isBetween(int x, int lower, int upper){
        return lower <= x && x <= upper;
    }

}
