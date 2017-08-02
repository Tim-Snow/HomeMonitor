package com.tim.filepointer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Stack;

@SpringBootApplication
public class Application {

    private     static      ApplicationContext      context;
    private     static      String                  latestImageName = "";
    private     static      Stack<String>           imageNames;
    private     static      WebcamRunner            webcamRunner;

    public static void main(String[] args) {

        context = SpringApplication.run(Application.class, args);

        imageNames = new Stack<>();

        if(GlobalValues.WEBCAM_ENABLED) {
            webcamRunner = new WebcamRunner(15000);

            webcamRunner.run();
        }

    }

    static {
        if(GlobalValues.WEBCAM_ENABLED) {
            Webcam.setDriver(new V4l4jDriver());
        }
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

    static String getImage(int i){ return imageNames.elementAt(i); }

    static int getTotalImages(){ return imageNames.size(); }

    static void exitSpring(){
        webcamRunner.stop();
        Util.initiateShutdown(context, 1);
    }
}
