package com.tim.filepointer;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

class Util {

    static void initiateShutdown(ApplicationContext context, int returnCode){

        //TODO move all images from session in to history folder

        SpringApplication.exit(context, () -> returnCode);
    }

    static boolean isBetween(int x, int lower, int upper){
        return lower <= x && x <= upper;
    }

}
