package com.tim.filepointer;

import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import static com.tim.filepointer.GlobalValues.EMAIL_ENABLED;

@Component
class EmailService {

    void sendEmail(Vector<String> imageNames) {
        if (EMAIL_ENABLED) {
            System.out.println("Sending email...");

            final String username = GlobalValues.EMAIL_ADDRESS;

            Properties props = new Properties();
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, GlobalValues.PASSWORD);
                        }
                    });

            try {

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(GlobalValues.TO_EMAIL_ADDRESS));
                message.setSubject("Motion Detected");

                Multipart multipart = new MimeMultipart();

                for (String imageName : imageNames) {

                    MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    mimeBodyPart.setFileName(imageName);
                    mimeBodyPart.attachFile(new File(imageName));

                    multipart.addBodyPart(mimeBodyPart);

                }

                message.setContent(multipart);

                Transport.send(message);

                System.out.println("Email sent.");

            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        } else
            System.out.println("Email disabled.");
    }
}
