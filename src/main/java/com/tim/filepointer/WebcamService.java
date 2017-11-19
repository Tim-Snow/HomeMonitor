package com.tim.filepointer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.awt.*;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static com.tim.filepointer.GlobalValues.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class WebcamService implements WebcamMotionListener {

    @Autowired
    private FileService fileService;

    @Autowired
    private EmailService emailService;
    private Webcam webcam;
    private Vector<String> currentMotionFileNames = new Vector<>();
    private Callable regularCaptureCallable, motionCaptureCallable, manualCaptureCallable;
    private ScheduledFuture<?> regularFuture, motionFuture;
    private ScheduledExecutorService executor;
    private boolean motionDetectionRunning = false;
    private boolean motionDetectedSinceLastCheck = false;

//    static {
//        Webcam.setDriver(new V4l4jDriver());
//    }

    @SuppressWarnings("unused")
    @PostConstruct
    public void init() {
        if (WEBCAM_ENABLED) {

//            Webcam.setDriver(new V4l4jDriver());
            webcam = Webcam.getDefault();
            webcam.setViewSize(new Dimension(640, 480));
            WebcamMotionDetector detector = new WebcamMotionDetector(webcam);

            webcam.setViewSize(new Dimension(640, 480));
            detector.setInterval(MOTION_CAPTURE_INTERVAL);
            detector.addMotionListener(this);

            webcam.open();
            detector.start();

            regularCaptureCallable = new CaptureCallable(fileService, webcam, "");
            motionCaptureCallable = new CaptureCallable(fileService, webcam, "MOTION_");
            manualCaptureCallable = new CaptureCallable(fileService, webcam, "MANUAL_");

            executor = new ScheduledThreadPoolExecutor(0);
            regularFuture = executor.scheduleAtFixedRate(regularTask(), 0, CAPTURE_INTERVAL, SECONDS);
        }
    }

    @SuppressWarnings("unused")
    @PreDestroy
    public void cleanup() {
        if (WEBCAM_ENABLED) {
            System.out.println("Webcam clean up.");
            motionFuture.cancel(true);
            regularFuture.cancel(true);
            executor.shutdown();
        }
    }

    @Override
    public void motionDetected(WebcamMotionEvent wme) {
        motionDetectedSinceLastCheck = true;

        if (WEBCAM_ENABLED && !motionDetectionRunning) {
            System.out.println(">>> MOTION DETECTED <<<");
            motionDetectionRunning = true;
            motionFuture = executor.scheduleAtFixedRate(motionTask(), 0, MOTION_CAPTURE_INTERVAL, MILLISECONDS);
        }
    }

    private void sendEmailAndCleanup() {
        emailService.sendEmail(currentMotionFileNames);
        motionDetectionRunning = false;
        currentMotionFileNames.clear();
        motionFuture.cancel(true);
    }

    boolean isMotionDetectedSinceLastCheck() {
        boolean r = motionDetectedSinceLastCheck;
        motionDetectedSinceLastCheck = false;
        return r;
    }

    String manualCapture() {
        String fileName = "";

        if (WEBCAM_ENABLED) {
            try {
                fileName = (String) manualCaptureCallable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return fileName;
    }

    private TimerTask regularTask() {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    regularCaptureCallable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private TimerTask motionTask() {
        return new TimerTask() {
            int iterations = 0;

            @Override
            public void run() {
                try {
                    if (iterations < MOTION_NUM_IMAGES_BEFORE_EMAILING) {
                        iterations++;
                        currentMotionFileNames.add((String) motionCaptureCallable.call());
                    } else
                        sendEmailAndCleanup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
