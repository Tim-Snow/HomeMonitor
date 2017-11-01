package com.tim.filepointer;

import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public class CaptureCallable implements Callable {

    private FileService fileService;
    private Webcam webcam;
    private String fileNamePrefix;

    CaptureCallable(FileService fileService, Webcam webcam, String fileNamePrefix) {
        this.fileService = fileService;
        this.webcam = webcam;
        this.fileNamePrefix = fileNamePrefix;
    }

    private String captureImage() {
        String fileName = Util.createImageName(fileNamePrefix);
        File file = new File(Util.fileNameBuilder(fileName));

        try {
            ImageIO.write(webcam.getImage(), "JPG", file);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        System.out.println("New image: " + fileName);
        fileService.addToImageNames(fileName);
        return fileName;
    }

    @Override
    public Object call() throws Exception {
        return captureImage();
    }
}
