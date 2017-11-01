package com.tim.filepointer;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

class Util {

    static void initiateShutdown(ApplicationContext context, int returnCode) {
        //cleanImageDirectory();

        SpringApplication.exit(context, () -> returnCode);
    }

    private static void cleanImageDirectory() {
        System.out.println("FILE CLEAN UP");
        File[] files = new File(".").listFiles();

        if (files != null)
            for (File file : files)
                if (file.isFile())
                    if (file.getName().endsWith(".jpg"))
                        if (file.getName().startsWith("MOTION")) {
                            file.renameTo(new File("storage/" + file.getName()));
                        } else {
                            System.out.println("DELETING: " + file.getName());
                            file.delete();
                        }
    }

    static String fileNameBuilder(String fileName) {
        return fileName + ".jpg";
    }

    static String createImageName(String prefix) {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Europe/London"));

        return prefix + formatImageName(localDateTime);
    }

    private static String formatImageName(LocalDateTime ldt) {
        return formatTime(ldt.getHour(), ldt.getMinute(), ldt.getSecond()) + "_" +
                formatDate(ldt.getDayOfMonth(), ldt.getMonthValue(), ldt.getYear());
    }

    private static String formatDate(int day, int month, int year) {
        return String.format("%02d", day) + "-" + String.format("%02d", month) + "-" + String.format("%02d", year);
    }

    private static String formatTime(int hour, int minute, int second) {
        return String.format("%02d", hour) + "-" + String.format("%02d", minute) + "-" + String.format("%02d", second);
    }

    static Map<String, String> buildResponse(String key, String value) {
        if(GlobalValues.WEBCAM_ENABLED){
            return Collections.singletonMap(key, value);
        } else {
            return buildWebcamNotEnabledResponse();
        }
    }

    static Map<String, String> buildMultiResponse(String key, Stack<String> responses) {
        if (GlobalValues.WEBCAM_ENABLED) {
            HashMap<String, String> responseMap = new HashMap<>();
            for (int i = 0; i < responses.size(); i++) {
                responseMap.put(key + " " + i, responses.get(i));
            }
            return responseMap;
        } else {
            return buildWebcamNotEnabledResponse();
        }
    }

    static Map<String, String> buildWebcamNotEnabledResponse(){
        return Collections.singletonMap("error", "Webcam not enabled.");
    }

    static Map<String, String> buildGeneralErrorResponse(){
        return Collections.singletonMap("error", "Something went wrong. :(");
    }

    static ResponseEntity<Object> buildResponseEntityWithImage(String filename){
        if(GlobalValues.WEBCAM_ENABLED) {
            try {
                File file = new File(Util.fileNameBuilder(filename));
                byte[] bytes = Files.readAllBytes(file.toPath());
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(bytes);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildGeneralErrorResponse());
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildWebcamNotEnabledResponse());
    }

}
