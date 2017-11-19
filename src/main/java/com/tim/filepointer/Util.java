package com.tim.filepointer;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static com.tim.filepointer.GlobalValues.WEBCAM_ENABLED;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static java.util.Collections.singletonMap;

class Util {

    static void exit(ApplicationContext context, int returnCode) {
        SpringApplication.exit(context, () -> returnCode);
    }

    static String jpg(String fileName) {
        return fileName + ".jpg";
    }

    static String createImageName(String prefix) {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Europe/London"));

        return prefix + formatImageName(localDateTime);
    }

    private static String formatImageName(LocalDateTime ldt) {
        return String.format("%s_%s", formatTime(ldt.getHour(), ldt.getMinute(), ldt.getSecond()),
                formatDate(ldt.getDayOfMonth(), ldt.getMonthValue(), ldt.getYear()));
    }

    private static String formatDate(int day, int month, int year) {
        return String.format("%s-%s-%s", format("%02d", day), format("%02d", month), format("%02d", year));
    }

    private static String formatTime(int hour, int minute, int second) {
        return String.format("%s-%s-%s", format("%02d", hour), format("%02d", minute), format("%02d", second));
    }

    static Map<String, String> buildResponse(String key, String value) {
        if(WEBCAM_ENABLED)
            return singletonMap(key, value);
         else
            return webcamNotEnabledResponse();
    }

    static Map<String, String> buildMultiResponse(String key, Deque<String> responses) {
        if (WEBCAM_ENABLED) {
            HashMap<String, String> responseMap = new HashMap<>();
            int count = 0;
            for(String response: responses){
                responseMap.put(key + " " + count, response);
                count++;
            }

            return responseMap;
        }

        return webcamNotEnabledResponse();
    }

    static ResponseEntity<Object> buildImageResponse(String filename){
        if(WEBCAM_ENABLED) {
            try {
                File file = new File(filename);
                byte[] bytes = readAllBytes(file.toPath());
                return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(generalErrorResponse());
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(webcamNotEnabledResponse());
    }

    private static Map<String, String> webcamNotEnabledResponse(){
        return singletonMap("error", "Webcam not enabled.");
    }

    private static Map<String, String> generalErrorResponse(){
        return singletonMap("error", "Something went wrong. :(");
    }

}
