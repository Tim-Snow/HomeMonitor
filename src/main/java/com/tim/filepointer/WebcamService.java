package com.tim.filepointer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Vector;

@Component
@EnableAutoConfiguration
class WebcamService extends Application implements WebcamMotionListener {

    @Autowired
    private FileService fileService;

    @Autowired
    private EmailService emailService;

    private boolean running, motionDetected = false, newMotionDetected = false;
    private long currentTimeMillis, nextImageTimeMillis, endTimeMillis;
    private Webcam webcam;

    @PostConstruct
    public void init() {
        setupWebcam();

        Runnable webcamService = this::imageCaptureLoop;
        new Thread(webcamService).start();
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

    private void imageCaptureLoop() {
        running = true;

        while (running) {
            captureImage();
            sleepThreadForCaptureDelay();
        }

        webcam.close();
    }

    private void captureImage() {
        String fileName = createImageName(false);

        fileService.addToImageNames(fileName);
        File file = new File(Util.fileNameBuilder(fileName));
        System.out.println("New image: " + fileName);

        try {
            ImageIO.write(webcam.getImage(), "JPG", file);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void sleepThreadForCaptureDelay() {
        try {
            final long CAPTURE_DELAY = 15000;
            Thread.sleep(CAPTURE_DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void motionDetected(WebcamMotionEvent wme) {

        newMotionDetected = true;

        if (!motionDetected && running) {

            System.out.println("MOTION DETECTED");
            motionDetected = true;

            Vector<String> fileNames = new Vector<>();

            currentTimeMillis = System.currentTimeMillis();
            endTimeMillis = currentTimeMillis + GlobalValues.MOTION_TIME_TO_WAIT_BEFORE_EMAILING;
            nextImageTimeMillis = currentTimeMillis + GlobalValues.MOTION_CAPTURE_INTERVAL;

            Runnable runnable = () -> {

                while (true) {
                    currentTimeMillis = System.currentTimeMillis();

                    if (currentTimeMillis >= nextImageTimeMillis && newMotionDetected) {

                        newMotionDetected = false;

                        nextImageTimeMillis = currentTimeMillis + GlobalValues.MOTION_CAPTURE_INTERVAL;
                        captureMotionImage(fileNames);
                    }

                    if (currentTimeMillis >= endTimeMillis) {
                        sendEmailAndCleanup(fileNames);
                        return;
                    }
                }
            };

            new Thread(runnable).start();
        }
    }

    private void captureMotionImage(Vector<String> fileNames) {
        String fileName = createImageName(true);

        try {
            File file = new File(Util.fileNameBuilder(fileName));
            ImageIO.write(webcam.getImage(), "JPG", file);
            System.out.println("New image: " + fileName);
            fileService.addToImageNames(fileName);
            fileNames.add(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendEmailAndCleanup(Vector<String> fileNames) {
        if (GlobalValues.EMAIL_ENABLED) {
            System.out.println("Sending email...");
            emailService.sendEmail(fileNames);
            fileNames.clear();
            motionDetected = false;
        } else {
            System.out.println("Email disabled.");
        }

        fileNames.clear();
        motionDetected = false;
    }

    private String createImageName(boolean motionDetected) {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Europe/London"));

        if (motionDetected) {
            return Util.createMotionImageName(localDateTime, localDateTime.getSecond());
        } else {
            if (Util.isBetween(localDateTime.getSecond(), 0, 14)) {
                return Util.createImageName(localDateTime, 0);
            } else if (Util.isBetween(localDateTime.getSecond(), 15, 29)) {
                return Util.createImageName(localDateTime, 1);
            } else if (Util.isBetween(localDateTime.getSecond(), 30, 44)) {
                return Util.createImageName(localDateTime, 2);
            } else if (Util.isBetween(localDateTime.getSecond(), 45, 60)) {
                return Util.createImageName(localDateTime, 3);
            } else {
                return Util.createImageName(localDateTime, 9);
            }
        }
    }
}
