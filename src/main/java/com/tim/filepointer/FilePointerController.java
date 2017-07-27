package com.tim.filepointer;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class FilePointerController {

    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/image")
    public FilePointer filePointer(@RequestParam(value="id", defaultValue="0") String id) {

        if(GlobalValues.WEBCAM_ENABLED) {

            int imageId;

            try {
                imageId = Integer.parseInt(id);
            } catch (NumberFormatException | NullPointerException e) {
                imageId = Application.getTotalImages() - 1;
            }

            return new FilePointer(counter.incrementAndGet(), Application.getImage(imageId));
        } else {
            return new FilePointer(counter.incrementAndGet(), "Not enabled.");
        }

    }

    @RequestMapping("/total_images")
    public FilePointer totalImages(){
        if(GlobalValues.WEBCAM_ENABLED) {
            return new FilePointer(counter.incrementAndGet(), String.valueOf(Application.getTotalImages()));
        } else {
            return new FilePointer(counter.incrementAndGet(), "Not enabled.");
        }
    }

    @RequestMapping("/latest_image")
    public FilePointer latestImage() {
        if(GlobalValues.WEBCAM_ENABLED) {
            return new FilePointer(counter.incrementAndGet(), Application.getLatestImageName());
        } else {
            return new FilePointer(counter.incrementAndGet(), "Not enabled.");
        }
    }

}
