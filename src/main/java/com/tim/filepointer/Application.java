package com.tim.filepointer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Stack;

@SpringBootApplication
public class Application {

    private     static      int                 maxImages = 3;

    private     static      ApplicationContext      context;
    private     static      String                  latestImageName = "";
    private     static      Stack<String>           imageNames;
    private     static      String[]                oldImageNames;

    public static void main(String[] args) {

        imageNames = new Stack<String>();

        //oldImageNames = new String[];

        context = SpringApplication.run(Application.class, args);

        WebcamRunner webcamRunner = new WebcamRunner(15000);

        webcamRunner.run();

    }

    public static void setLatestImageName(String s){
        imageNames.push(s);

        if (imageNames.size() > maxImages){
            System.out.println("Removing from history: " + imageNames.elementAt(0));
            imageNames.remove(0);
        }

        latestImageName = s;
    }

    public static String getLatestImageName(){
        return latestImageName;
    }

    public static void exitSpring(){
        Util.initiateShutdown(context, 1);
    }

}
