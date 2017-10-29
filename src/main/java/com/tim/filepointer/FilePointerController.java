package com.tim.filepointer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Map;

@RestController
public class FilePointerController {

    @Autowired
    private FileService fileService;

    @Autowired
    private WebcamService webcamService;

    @GetMapping("/total_images")
    public Map<String, String> getTotalImages() {
        if (GlobalValues.WEBCAM_ENABLED) {
            return Util.buildResponse("total_images", String.valueOf(fileService.getTotalImages()));
        } else {
            return Util.buildResponse("error", "Webcam not enabled.");
        }
    }

    @GetMapping("/name/all_images")
    public Map<String, String> getAllImageNames() {
        return Util.buildMultiResponse("file", fileService.getAllImageNames());
    }

    @GetMapping("/name/latest")
    public Map<String, String> getLatestImageName() {
        if (GlobalValues.WEBCAM_ENABLED) {
            return Util.buildResponse("file", fileService.getLatestImageName());
        } else {
            return Util.buildResponse("error", "Webcam not enabled.");
        }
    }

    @GetMapping(value = "/image/manual_capture", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImageManually(HttpServletRequest request) throws Exception {
        if (GlobalValues.WEBCAM_ENABLED) {
            File file = new File(Util.fileNameBuilder(webcamService.manualCapture()));
            byte[] image = Files.readAllBytes(file.toPath());
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
        } else {
            throw new Exception("Webcam not enabled.");
        }
    }

    @GetMapping(value = "/image/latest", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getLatestImage(HttpServletRequest request) throws Exception {
        if (GlobalValues.WEBCAM_ENABLED) {
            File file = new File(Util.fileNameBuilder(fileService.getLatestImageName()));
            byte[] image = Files.readAllBytes(file.toPath());
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
        } else {
            throw new Exception("Webcam not enabled.");
        }
    }

    @GetMapping(value = "/image/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImageByFilename(HttpServletRequest request, @PathVariable("fileName") String fileName) throws Exception {
        if (GlobalValues.WEBCAM_ENABLED) {
            try {
                File file = new File(Util.fileNameBuilder(fileName));
                byte[] image = Files.readAllBytes(file.toPath());
                return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
            } catch (NoSuchFileException e) {
                throw new Exception("File not found.");
            }
        } else {
            throw new Exception("Webcam not enabled.");
        }
    }
}
