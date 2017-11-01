package com.tim.filepointer

import com.tim.filepointer.Util.buildResponseEntityWithImage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class FilePointerController @Autowired constructor(private val fileService: FileService, private val webcamService: WebcamService) {

    @GetMapping("/total_images")
    fun totalImages(): Map<String, String> = Util.buildResponse("total_images", fileService.totalImages.toString())

    @GetMapping("/name/all_images")
    fun allImageNames(): Map<String, String> = Util.buildMultiResponse("file_name", fileService.allImageNames)

    @GetMapping("/name/latest")
    fun latestImageName(): Map<String, String> = Util.buildResponse("file_name", fileService.latestImageName)

    @GetMapping("/image/manual_capture")
    fun manualCaptureImage(): ResponseEntity<Any> = buildResponseEntityWithImage(webcamService.manualCapture())

    @GetMapping("/image/latest")
    fun latestImage(): ResponseEntity<Any> = buildResponseEntityWithImage(fileService.latestImageName)

    @GetMapping("/image/{fileName}")
    fun getImageByName(@PathVariable("fileName") fileName: String): ResponseEntity<Any> = buildResponseEntityWithImage(fileName)
}