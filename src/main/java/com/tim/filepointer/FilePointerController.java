package com.tim.filepointer;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

@RestController
public class FilePointerController {

    @GetMapping("/image_name/{id}")
    public Map<String, String> imageName(@RequestParam(value="id", defaultValue="0") String id) {
        if(GlobalValues.WEBCAM_ENABLED) {
            int imageId;
            try {
                imageId = Integer.parseInt(id);
            } catch (NumberFormatException | NullPointerException e) {
                imageId = Application.getTotalImages() - 1;
            }

            return buildResponse(Application.getImage(imageId));
        } else {
            return buildResponse("Not enabled.");
        }
    }

    @GetMapping("/total_image_count")
    public Map<String, String> totalImages(){
        if(GlobalValues.WEBCAM_ENABLED) {
            return buildResponse(String.valueOf(Application.getTotalImages()));
        } else {
            return buildResponse("Not enabled.");
        }
    }

    @GetMapping("/latest_image_name")
    public Map<String, String> latestImageName() {
        if(GlobalValues.WEBCAM_ENABLED) {
            return buildResponse(Application.getLatestImageName());
        } else {
            return buildResponse("Not enabled.");
        }
    }

    @GetMapping(value = "/get_latest_image", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getLatestImage(HttpServletRequest request) throws Exception {
        File fi = new File("images/a.jpeg");
        byte[] image = Files.readAllBytes(fi.toPath());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
    }

    private Map<String, String> buildResponse(String response){
        return Collections.singletonMap("Response", response);
    }
}
