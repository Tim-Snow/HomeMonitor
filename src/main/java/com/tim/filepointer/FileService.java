package com.tim.filepointer;

import org.springframework.stereotype.Component;

import java.util.Stack;

@Component
public class FileService {

    private static String latestImageName = "";
    private static Stack<String> imageNames;

    public FileService() {
        imageNames = new Stack<>();
    }

    void setLatestImageName(String s) {
        imageNames.push(s);

        if (imageNames.size() > GlobalValues.MAX_IMAGES) {
            System.out.println("Removing from history: " + imageNames.elementAt(0));
            imageNames.remove(0);
        }

        latestImageName = s;
    }

    String getLatestImageName() {
        return latestImageName;
    }

    String getImage(int i) {
        return imageNames.elementAt(i);
    }

    int getTotalImages() {
        return imageNames.size();
    }
}
