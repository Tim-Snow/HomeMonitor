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
import java.util.Timer;
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
    private Thread motionCaptureThread;
    private Callable webcamCallable;
    private ScheduledExecutorService executor;
    private volatile boolean motionDetectionRunning = false;

    @PostConstruct
    @SuppressWarnings("unused")
    public void init() {
        Webcam.getDefault().close();
        setupWebcam();

        webcamCallable = new CaptureRunnable(fileService, webcam);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                try {
                    System.out.println(webcamCallable.call());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        executor = new ScheduledThreadPoolExecutor(4);
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(task, 0, 15, TimeUnit.SECONDS);
    }

    @PreDestroy
    @SuppressWarnings("unused")
    public void cleanup(){
        System.out.println("WEBCAM CLEAN UP");
        motionDetectionRunning = false;
        executor.shutdown();
        motionCaptureThread.interrupt();
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
        if (motionCaptureThread == null) {
            motionDetectionRunning = true;
            System.out.println(">>>MOTION DETECTED<<<");
            motionCaptureThread = new Thread(motionDetectionRunnable());
            motionCaptureThread.start();
        }
    }

    private void captureImage() {
        String fileName;

        fileName = Util.createImageName(true);
        currentMotionFileNames.add(fileName);

        fileService.addToImageNames(fileName);
        File file = new File(Util.fileNameBuilder(fileName));
        System.out.println("New image: " + fileName);

        try {
            ImageIO.write(webcam.getImage(), "JPG", file);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private Runnable motionDetectionRunnable() {
        long endTimeMillis = System.currentTimeMillis() + GlobalValues.MOTION_TIME_TO_WAIT_BEFORE_EMAILING;

        return () -> {
            while (motionDetectionRunning) {
                captureImage();

                try {
                    motionCaptureThread.sleep(GlobalValues.MOTION_CAPTURE_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (System.currentTimeMillis() >= endTimeMillis)
                    sendEmailAndCleanup();
            }
        };
    }

    private void sendEmailAndCleanup() {
        if (GlobalValues.EMAIL_ENABLED) {
            System.out.println("Sending email...");
            emailService.sendEmail(currentMotionFileNames);
        } else
            System.out.println("Email disabled.");

        motionDetectionRunning = false;
        motionCaptureThread = null;
        currentMotionFileNames.clear();
    }
}
