package com.tim.filepointer;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.*;

class Util {

    static void initiateShutdown(ApplicationContext context, int returnCode) {

        //TODO move all images from session in to history folder

        SpringApplication.exit(context, () -> returnCode);
    }

    static String fileNameBuilder(String fileName) {
        return "images\\" + fileName + ".jpg";
    }

    static String createImageName(LocalDateTime ldt, int id) {
        return  formatTime(ldt.getHour(), ldt.getMinute(), ldt.getSecond()) + "_" +
                formatDate(ldt.getDayOfMonth(), ldt.getMonthValue(), ldt.getYear()) +
                "_" + Integer.toString(id);
    }

    static String createMotionImageName(LocalDateTime ldt, int id) {
        return "MOTION_" +
                formatTime(ldt.getHour(), ldt.getMinute(), ldt.getSecond()) + "_" +
                formatDate(ldt.getDayOfMonth(), ldt.getMonthValue(), ldt.getYear()) +
                "_" + Integer.toString(id);
    }

    private static String formatDate(int day, int month, int year) {
        return String.format("%02d", day) + "-" + String.format("%02d", month) + "-" + String.format("%02d", year);
    }

    private static String formatTime(int hour, int minute, int second) {
        return String.format("%02d", hour) + "-" + String.format("%02d", minute) + "-" + String.format("%02d", second);
    }

    static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

    static Map<String, String> buildResponse(String response) {
        return Collections.singletonMap("response", response);
    }

    static Map<String, String> buildResponse(String key, String response) {
        return Collections.singletonMap(key, response);
    }

    static Map<String, String> buildMultiResponse(Vector<String> responses) {
        HashMap<String, String> responseMap = new HashMap<>();

        for (int i = 0; i < responses.size(); i++) {
            responseMap.put("response " + i, responses.get(i));
        }

        return responseMap;
    }

    static Map<String, String> buildMultiResponse(String key, Stack<String> responses) {
        HashMap<String, String> responseMap = new HashMap<>();

        for (int i = 0; i < responses.size(); i++) {
            responseMap.put(key + " " + i, responses.get(i));
        }

        return responseMap;
    }

}
