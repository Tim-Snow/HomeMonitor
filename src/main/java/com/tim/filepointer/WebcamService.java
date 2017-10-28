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

    private boolean running, motionDetected, newMotionDetected;
    private long delay, currentTimeMillis, nextImageTimeMillis, endTimeMillis;
    private Webcam webcam;
    private LocalDateTime localDateTime;

    @PostConstruct
    public void init() {

        this.delay = 15000;

        motionDetected = false;
        newMotionDetected = false;

        webcam = Webcam.getDefault();
        WebcamMotionDetector detector = new WebcamMotionDetector(webcam);

        webcam.setViewSize(new Dimension(640, 480));
        detector.setInterval(GlobalValues.MOTION_CAPTURE_INTERVAL);
        detector.addMotionListener(this);

        webcam.open();
        detector.start();

        Runnable startWebcamService = this::start;

        new Thread(startWebcamService).start();

    }

    private String createFileName(LocalDateTime ldt, boolean motion, int id) {
        if (motion) {
            return "MOTION_" + String.format("%02d", ldt.getHour()) + "-" + String.format("%02d", ldt.getMinute()) +
                    "-" + String.format("%02d", ldt.getSecond()) + "_" + String.format("%02d", ldt.getDayOfMonth()) + "-" + String.format("%02d", ldt.getMonthValue()) +
                    "-" + String.format("%02d", ldt.getYear()) + "_" + Integer.toString(id);
        } else {
            return String.format("%02d", ldt.getHour()) + "-" + String.format("%02d", ldt.getMinute()) +
                    "_" + String.format("%02d", ldt.getDayOfMonth()) + "-" + String.format("%02d", ldt.getMonthValue()) +
                    "-" + String.format("%02d", ldt.getYear()) + "_" + Integer.toString(id);
        }
    }

    private void start() {
        running = true;

        Long delayTimeMillis = System.currentTimeMillis();

        while (running) {

            if (System.currentTimeMillis() >= delayTimeMillis) {

                delayTimeMillis = System.currentTimeMillis() + delay;

                localDateTime = LocalDateTime.now(ZoneId.of("Europe/London"));

                String fileName = "";

                if (Util.isBetween(localDateTime.getSecond(), 0, 14)) {
                    fileName = createFileName(localDateTime, false, 0);
                } else if (Util.isBetween(localDateTime.getSecond(), 15, 29)) {
                    fileName = createFileName(localDateTime, false, 1);
                } else if (Util.isBetween(localDateTime.getSecond(), 30, 44)) {
                    fileName = createFileName(localDateTime, false, 2);
                } else if (Util.isBetween(localDateTime.getSecond(), 45, 60)) {
                    fileName = createFileName(localDateTime, false, 3);
                }

                fileService.setLatestImageName(fileName);
                System.out.println("New image: " + fileName);
                File file = new File("images/" + fileName + ".jpg");

                try {
                    ImageIO.write(webcam.getImage(), "JPG", file);
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }

            }
        }

        webcam.close();
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

                    localDateTime = LocalDateTime.now(ZoneId.of("Europe/London"));
                    currentTimeMillis = System.currentTimeMillis();

                    if (currentTimeMillis >= nextImageTimeMillis && newMotionDetected) {

                        newMotionDetected = false;

                        nextImageTimeMillis = currentTimeMillis + GlobalValues.MOTION_CAPTURE_INTERVAL;
                        String fileName = createFileName(localDateTime, true, localDateTime.getSecond());


                        try {
                            File file = new File(fileName);
                            ImageIO.write(webcam.getImage(), "JPG", file);
                            System.out.println("New image: " + fileName);
                            fileService.setLatestImageName(fileName);
                            fileNames.add(fileName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (currentTimeMillis >= endTimeMillis) {
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
                        return;
                    }
                }
            };

            Thread thread = new Thread(runnable);
            thread.start();

        }
    }
}
