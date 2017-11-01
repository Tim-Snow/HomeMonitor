package com.tim.filepointer;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Stack;
import java.util.Vector;

@Component
public class FileService {

    private int totalImages = 0;
    private Stack<String> imageNames = new Stack<>();

    private FileService() {
//        cleanOldFiles();
    }

    private void cleanOldFiles() {
        File[] files = new File(".").listFiles();
        Vector<File> vFiles = new Vector<>();

        vFiles.addAll(Arrays.asList(files));

        for(File file: vFiles){
            if (file != null) {
                if (file.getName().endsWith(".jpg")) {
                    totalImages++;

                    if (totalImages >= GlobalValues.MAX_IMAGES) {
                        deleteOrStoreJpg(vFiles.firstElement(), vFiles);
                    }
                }
            }
        }
    }

    private void deleteOrStoreJpg(File file, Vector<File> files) {
        if (file.getName().startsWith("MOTION_") || file.getName().startsWith("MANUAL_")) {
            System.out.println("Moving: " + file.getName() + " to storage.");
            file.renameTo(new File("storage/" + file.getName()));
            files.remove(0);
            totalImages--;
        } else {
            System.out.println("Deleting: " + file.getName());
            file.delete();
            files.remove(file);
            totalImages--;
        }
    }

    private void deleteOrStoreJpg(String fileName) {
        File file = new File(fileName);
        if (file.getName().startsWith("MOTION_") || file.getName().startsWith("MANUAL_")) {
            System.out.println("Moving: " + file.getName() + " to storage.");
            file.renameTo(new File("storage/" + file.getName()));
        } else {
            System.out.println("Deleting: " + file.getName());
            file.delete();
            totalImages--;
        }
    }

    void addToImageNames(String imageName) {
        imageNames.push(imageName);
        totalImages++;

        if (imageNames.size() > GlobalValues.MAX_IMAGES || totalImages > GlobalValues.MAX_IMAGES) {
            deleteOrStoreJpg(imageName);
            imageNames.remove(0);
        }
    }

    String getLatestImageName() {
        if(imageNames.size() > 0)
            return imageNames.peek();
        return "No images.";
    }

    Stack<String> getAllImageNames() {
        return imageNames;
    }

    int getTotalImages() {
        return imageNames.size();
    }
}
