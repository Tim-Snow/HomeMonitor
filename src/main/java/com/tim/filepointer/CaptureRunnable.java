package com.tim.filepointer;

import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public class CaptureRunnable implements Callable {

    private FileService fileService;
    private Webcam webcam;
    private boolean motionMode;

    CaptureRunnable(FileService fileService, Webcam webcam, boolean motionMode) {
        this.fileService = fileService;
        this.webcam = webcam;
        this.motionMode = motionMode;
    }

    private String captureImage() {
        String fileName;

        fileName = Util.createImageName(motionMode);
        fileService.addToImageNames(fileName);
        File file = new File(Util.fileNameBuilder(fileName));

        try {
            ImageIO.write(webcam.getImage(), "JPG", file);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        return "New image: " + fileName;
    }

    @Override
    public Object call() throws Exception {
        return captureImage();
    }
}
