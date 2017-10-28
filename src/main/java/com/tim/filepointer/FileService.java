package com.tim.filepointer;

import org.springframework.stereotype.Component;

import java.util.Stack;

@Component
class FileService {

    private Stack<String> imageNames = new Stack<>();

    void setLatestImageName(String s) {
        imageNames.push(s);

        if (imageNames.size() > GlobalValues.MAX_IMAGES) {
            System.out.println("Removing from history: " + imageNames.elementAt(0));
            imageNames.remove(0);
        }
    }

    String getLatestImageName() {
        return imageNames.peek();
    }

    Stack<String> getAllImageNames() {
        return imageNames;
    }

    int getTotalImages() {
        return imageNames.size();
    }
}
