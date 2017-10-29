package com.tim.filepointer;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

class Util {

    static void initiateShutdown(ApplicationContext context, int returnCode) {
        cleanImageDirectory();

        SpringApplication.exit(context, () -> returnCode);
    }

    private static void cleanImageDirectory() {
        System.out.println("FILE CLEAN UP");
        File[] files = new File("images").listFiles();

        if (files != null)
            for (File file : files)
                if (file.isFile())
                    if (file.getName().endsWith(".jpg"))
                        if (file.getName().startsWith("MOTION")) {
                            file.renameTo(new File("images/motion_storage" + file.getName()));
                        } else {
                            System.out.println("DELETING: " + file.getName());
                            file.delete();
                        }
    }

    static String fileNameBuilder(String fileName) {
        return "images\\" + fileName + ".jpg";
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

    static Map<String, String> buildResponse(String key, String response) {
        return Collections.singletonMap(key, response);
    }

    static Map<String, String> buildMultiResponse(String key, Stack<String> responses) {
        HashMap<String, String> responseMap = new HashMap<>();

        for (int i = 0; i < responses.size(); i++) {
            responseMap.put(key + " " + i, responses.get(i));
        }

        return responseMap;
    }

}
