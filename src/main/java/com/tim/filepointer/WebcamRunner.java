package com.tim.filepointer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;
import java.util.Vector;

public class WebcamRunner extends Application implements WebcamMotionListener {

    private final int MOTION_TIME_TO_WAIT_BEFORE_EMAILING = 15000; //15 Seconds
    private final int MOTION_CAPTURE_INTERVAL = 500; //0.5 Second

    private     boolean             running, motionDetected, newMotionDetected;
    private     long                delay, currentTimeMillis, nextImageTimeMillis, endTimeMillis;
    private     Webcam              webcam;
    private     LocalDateTime       localDateTime;
    private     Thread              thread;

    public WebcamRunner(long delay){

        this.delay = delay;

        motionDetected = false;
        newMotionDetected = false;

        webcam = Webcam.getDefault();
        WebcamMotionDetector detector = new WebcamMotionDetector(webcam);

        webcam.setViewSize(new Dimension(640, 480));
        detector.setInterval(MOTION_CAPTURE_INTERVAL);
        detector.addMotionListener(this);

        webcam.open();
        detector.start();

    }

    private String createFileName(LocalDateTime ldt, boolean motion, int id){
        if(motion){
            return "images/" + "MOTION_" + String.format("%02d", ldt.getHour()) + "-" + String.format("%02d", ldt.getMinute()) +
                    "-" + String.format("%02d", ldt.getSecond()) + "_" + String.format("%02d", ldt.getDayOfMonth()) + "-" + String.format("%02d", ldt.getMonthValue()) +
                    "-" + String.format("%02d", ldt.getYear()) + "_" + Integer.toString(id) + ".jpg";
        } else {
            return "images/" + String.format("%02d", ldt.getHour()) + "-" + String.format("%02d", ldt.getMinute()) +
                    "_" + String.format("%02d", ldt.getDayOfMonth()) + "-" + String.format("%02d", ldt.getMonthValue()) +
                    "-" + String.format("%02d", ldt.getYear()) + "_" + Integer.toString(id) + ".jpg";
        }
    }

    public void run(){
        running = true;

        currentTimeMillis = System.currentTimeMillis();

        while(running) {

            if(System.currentTimeMillis() >= currentTimeMillis) {

                currentTimeMillis = System.currentTimeMillis() + delay;

                localDateTime = LocalDateTime.now(ZoneId.of("Europe/London"));

                String fileName = "";

                if (Util.isBetween(localDateTime.getSecond(), 0, 14)){
                    fileName = createFileName(localDateTime, false, 0);
                } else if (Util.isBetween(localDateTime.getSecond(), 15, 29)){
                    fileName = createFileName(localDateTime, false, 1);
                } else if (Util.isBetween(localDateTime.getSecond(), 30, 44)){
                    fileName = createFileName(localDateTime, false, 2);
                } else if (Util.isBetween(localDateTime.getSecond(), 45, 60)){
                    fileName = createFileName(localDateTime, false, 3);
                }

                File file = new File(fileName);
                System.out.println("New image: " + fileName);
                Application.setLatestImageName(fileName);

                try {
                    ImageIO.write(webcam.getImage(), "JPG", file);
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }

            }
        }

        webcam.close();
        Application.exitSpring();
    }

    public void stop(){
        running = false;
    }

    @Override
    public void motionDetected(WebcamMotionEvent wme) {

        newMotionDetected = true;

        if(!motionDetected) {

            System.out.println("MOTION DETECTED");
            motionDetected = true;

            Vector<String> fileNames    = new Vector<>();

            currentTimeMillis           = System.currentTimeMillis();
            endTimeMillis               = currentTimeMillis + MOTION_TIME_TO_WAIT_BEFORE_EMAILING;
            nextImageTimeMillis         = currentTimeMillis + MOTION_CAPTURE_INTERVAL;

            Runnable runnable = () -> {

                while(true) {

                    localDateTime     = LocalDateTime.now(ZoneId.of("Europe/London"));
                    currentTimeMillis = System.currentTimeMillis();

                    if (currentTimeMillis >= nextImageTimeMillis && newMotionDetected) {

                        newMotionDetected = false;

                        nextImageTimeMillis = currentTimeMillis + MOTION_CAPTURE_INTERVAL;
                        String fileName = createFileName(localDateTime, true, localDateTime.getSecond());
                        File file = new File(fileName);

                        try {
                            ImageIO.write(webcam.getImage(), "JPG", file);
                            System.out.println("New image: " + fileName);
                            Application.setLatestImageName(fileName);
                            fileNames.add(fileName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (currentTimeMillis >= endTimeMillis) {
                        System.out.println("Sending email");
                        sendEmail(fileNames);
                        motionDetected = false;
                        return;
                    }

                }
            };

            thread = new Thread(runnable);
            thread.start();

        }
    }

    private void sendEmail(Vector<String> imageNames) {

        final String username = GlobalValues.EMAIL_ADDRESS;
        final String password = GlobalValues.PASSWORD;

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("tim.snow1991@gmail.com"));
            message.setSubject("Motion Detected");

            Multipart multipart = new MimeMultipart();

            for(String str : imageNames){

                MimeBodyPart mimeBodyPart = new MimeBodyPart();

                FileDataSource fileDataSource = new FileDataSource(str);
                mimeBodyPart.setDataHandler(new DataHandler(fileDataSource));
                mimeBodyPart.setFileName(str);
                mimeBodyPart.attachFile(new File(str));

                multipart.addBodyPart(mimeBodyPart);

            }

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Email sent re motion");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
