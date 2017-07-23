package com.tim.filepointer;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class FilePointerController {

    private static final String template = "Image URL: %s";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/image")
    public FilePointer filePointer(@RequestParam(value="id", defaultValue="0") String id) {
        return new FilePointer(counter.incrementAndGet(),
                String.format(template, Application.getImage(id)));
    }

    @RequestMapping("/latest_image")
    public FilePointer filePointer() {
        return new FilePointer(counter.incrementAndGet(),
                String.format(template, Application.getLatestImageName()));
    }

}
