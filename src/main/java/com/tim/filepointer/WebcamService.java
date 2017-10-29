package com.tim.filepointer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.awt.*;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.*;

@Component
@EnableAutoConfiguration
class WebcamService implements WebcamMotionListener {

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

    @SuppressWarnings("unused")
    @PostConstruct
    public void init() {
        setupWebcam();

        regularCaptureCallable = new CaptureCallable(fileService, webcam, "");
        motionCaptureCallable = new CaptureCallable(fileService, webcam, "MOTION_");
        manualCaptureCallable = new CaptureCallable(fileService, webcam, "MANUAL_");

        executor = new ScheduledThreadPoolExecutor(0);
        regularFuture = executor.scheduleAtFixedRate(getRegularTask(), 0, GlobalValues.CAPTURE_INTERVAL, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unused")
    @PreDestroy
    public void cleanup() {
        System.out.println("WEBCAM CLEAN UP");
        motionFuture.cancel(true);
        regularFuture.cancel(true);
        executor.shutdown();
        webcam.close();
    }

    private void setupWebcam() {
        webcam = Webcam.getDefault();
        WebcamMotionDetector detector = new WebcamMotionDetector(webcam);

        webcam.setViewSize(new Dimension(640, 480));
        detector.setInterval(GlobalValues.MOTION_CAPTURE_INTERVAL);
        detector.addMotionListener(this);

        webcam.open();
        detector.start();
    }

    @Override
    public void motionDetected(WebcamMotionEvent wme) {
        if (!motionDetectionRunning) {
            System.out.println(">>>MOTION DETECTED<<<");

            motionDetectionRunning = true;

            motionFuture = executor.scheduleAtFixedRate(getMotionTask(), 0, GlobalValues.MOTION_CAPTURE_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    private void sendEmailAndCleanup() {
        if (GlobalValues.EMAIL_ENABLED) {
            System.out.println("Sending email...");
            emailService.sendEmail(currentMotionFileNames);
        } else
            System.out.println("Email disabled.");

        motionDetectionRunning = false;
        currentMotionFileNames.clear();
        motionFuture.cancel(true);
    }

    String manualCapture() {
        String fileName = "";
        try {
            fileName = (String) manualCaptureCallable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileName;
    }

    private TimerTask getRegularTask() {
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

    private TimerTask getMotionTask() {
        return new TimerTask() {
            int iterations = 0;

            @Override
            public void run() {
                try {
                    if (iterations < GlobalValues.MOTION_NUM_IMAGES_BEFORE_EMAILING) {
                        iterations++;
                        currentMotionFileNames.add((String) motionCaptureCallable.call());
                    } else {
                        sendEmailAndCleanup();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
