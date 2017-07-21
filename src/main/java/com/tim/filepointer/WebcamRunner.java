package com.tim.filepointer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;

public class WebcamRunner extends Application implements WebcamMotionListener {

    private     boolean             running;
    private     long                delay;
    private     Webcam              webcam;
    private     LocalDateTime       localDateTime;

    public WebcamRunner(long delay){

        this.delay = delay;

        webcam = Webcam.getDefault();
        WebcamMotionDetector detector = new WebcamMotionDetector(webcam);

        webcam.setViewSize(new Dimension(640, 480));
        //TODO: get all images captured during motion and attach to a single email once motion stops or after timer
        detector.setInterval(500);
        detector.addMotionListener(this);

        webcam.open();
        detector.start();

    }

    private String createFileName(LocalDateTime ldt, boolean motion){
        if(motion){
            return "images/" + "MOTION_" + String.format("%02d", ldt.getHour()) + "-" + String.format("%02d", ldt.getMinute()) + "-" + String.format("%02d", ldt.getSecond())
                    + "_" + String.format("%02d", ldt.getDayOfMonth()) + "-" + String.format("%02d", ldt.getMonthValue()) + "-" + String.format("%02d", ldt.getYear());
        } else {
            return "images/" + String.format("%02d", ldt.getHour()) + "-" + String.format("%02d", ldt.getMinute())
                    + "_" + String.format("%02d", ldt.getDayOfMonth()) + "-" + String.format("%02d", ldt.getMonthValue()) + "-" + String.format("%02d", ldt.getYear());
        }
    }

    public void run(){
        running = true;

        long currentTimeMillis = System.currentTimeMillis();

        while(running) {

            if(System.currentTimeMillis() >= currentTimeMillis) {

                currentTimeMillis = System.currentTimeMillis() + delay;

                localDateTime = LocalDateTime.now(ZoneId.of("Europe/London"));

                String fileName = createFileName(localDateTime, false);

                if (Util.isBetween(localDateTime.getSecond(), 0, 14)){
                    //TODO: JPG in create file name func, add param for num
                    fileName += "_0.jpg";
                } else if (Util.isBetween(localDateTime.getSecond(), 15, 29)){
                    fileName += "_1.jpg";
                } else if (Util.isBetween(localDateTime.getSecond(), 30, 44)){
                    fileName += "_2.jpg";
                } else if (Util.isBetween(localDateTime.getSecond(), 45, 60)){
                    fileName += "_3.jpg";
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

        localDateTime = LocalDateTime.now(ZoneId.of("Europe/London"));
        String fileName = createFileName(localDateTime, true);

        fileName += ".jpg";

        File file = new File(fileName);
        System.out.println("MOTION DETECTED: " + fileName);

        try {
            ImageIO.write(webcam.getImage(), "JPG", file);
            Application.setLatestImageName(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendEmail(fileName);

    }

    private void sendEmail(String imageName) {
        DataSource dataSource = new FileDataSource(imageName);

        final String username = Util.EMAIL_ADDRESS;
        final String password = Util.PASSWORD;

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
            message.setText("Warning - Motion in home detected, image attached.");
            message.setDataHandler(new DataHandler(dataSource));
            message.setFileName(imageName);

            Transport.send(message);

            System.out.println("Email sent re motion");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
