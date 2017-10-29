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
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.*;

@Component
@EnableAutoConfiguration
class WebcamService extends Application implements WebcamMotionListener {

    @Autowired
    private FileService fileService;

    @Autowired
    private EmailService emailService;

    private Webcam webcam;
    private Vector<String> currentMotionFileNames = new Vector<>();
    private Callable regularCaptureCallable, motionCaptureCallable;
    private ScheduledFuture<?> regularFuture, motionFuture;
    private ScheduledExecutorService executor;
    private boolean motionDetectionRunning = false;

    @SuppressWarnings("unused")
    @PostConstruct
    public void init() {
        setupWebcam();

        motionCaptureCallable = new CaptureRunnable(fileService, webcam, true);
        regularCaptureCallable = new CaptureRunnable(fileService, webcam, false);

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                try {
                    System.out.println(regularCaptureCallable.call());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        executor = new ScheduledThreadPoolExecutor(0);
        regularFuture = executor.scheduleAtFixedRate(task, 0, GlobalValues.CAPTURE_INTERVAL, TimeUnit.SECONDS);
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

            TimerTask task = new TimerTask() {

                int iterations = 0;

                @Override
                public void run() {
                    try {
                        if (iterations < GlobalValues.MOTION_NUM_IMAGES_BEFORE_EMAILING) {
                            iterations++;
                            motionCaptureCallable.call();
                            System.out.println(motionCaptureCallable.call());
                        } else {
                            sendEmailAndCleanup();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            motionFuture = executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
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

    public String manualCapture() {
        String fileName;

        fileName = "MANUAL_" + Util.createImageName(false);
        fileService.addToImageNames(fileName);
        File file = new File(Util.fileNameBuilder(fileName));
        System.out.println("Manual capture: " + fileName);

        try {
            ImageIO.write(webcam.getImage(), "JPG", file);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        return fileName;
    }
}
