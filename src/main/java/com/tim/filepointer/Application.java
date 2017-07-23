package com.tim.filepointer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Stack;

@SpringBootApplication
public class Application {

    private     static      ApplicationContext      context;
    private     static      String                  latestImageName = "";
    private     static      Stack<String>           imageNames;

    public static void main(String[] args) {

        imageNames = new Stack<>();

        context = SpringApplication.run(Application.class, args);

        WebcamRunner webcamRunner = new WebcamRunner(15000);

        webcamRunner.run();

    }

    static void setLatestImageName(String s){
        imageNames.push(s);

        if (imageNames.size() > GlobalValues.MAX_IMAGES){
            System.out.println("Removing from history: " + imageNames.elementAt(0));
            imageNames.remove(0);
        }

        latestImageName = s;
    }

    static String getLatestImageName(){
        return latestImageName;
    }

    static void exitSpring(){
        Util.initiateShutdown(context, 1);
    }

}
