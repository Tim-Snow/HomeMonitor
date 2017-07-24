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

        int imageId;

        if(id.equals("total")){
            return new FilePointer(counter.incrementAndGet(), String.valueOf(Application.getTotalImages()));
        } else {
            try {
                imageId = Integer.parseInt(id);
            } catch (NumberFormatException | NullPointerException e){
                imageId = Application.getTotalImages() - 1;
            }

            return new FilePointer(counter.incrementAndGet(), Application.getImage(imageId));
        }
    }

    @RequestMapping("/latest_image")
    public FilePointer filePointer() {
        return new FilePointer(counter.incrementAndGet(), Application.getLatestImageName());
    }

}
