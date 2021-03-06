package com.tim.filepointer;

import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.tim.filepointer.Util.createImageName;
import static com.tim.filepointer.Util.jpg;

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
        String fileName = jpg(createImageName(fileNamePrefix));
        File file = new File(fileName);

        try {
            ImageIO.write(webcam.getImage(), "JPG", file);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        fileService.addToImageNames(fileName);
        return fileName;
    }

    @Override
    public Object call() throws Exception {
        return captureImage();
    }
}