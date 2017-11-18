package com.tim.filepointer

import com.tim.filepointer.Util.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ImageController @Autowired constructor(private val fileService: FileService, private val webcamService: WebcamService) {

    @GetMapping("/total_images")
    fun totalImages(): Map<String, String> = buildResponse("total_images", fileService.totalImages.toString())

    @GetMapping("/name/all_images")
    fun allImageNames(): Map<String, String> = buildMultiResponse("file_name", fileService.imageNames)

    @GetMapping("/name/latest")
    fun latestImageName(): Map<String, String> = buildResponse("file_name", fileService.getLatestImageName())

    @GetMapping("/image/manual_capture")
    fun manualCaptureImage(): ResponseEntity<Any> = buildImageResponse(webcamService.manualCapture())

    @GetMapping("/image/latest")
    fun latestImage(): ResponseEntity<Any> = buildImageResponse(fileService.getLatestImageName())

    @GetMapping("/image/{fileName}")
    fun getImageByName(@PathVariable("fileName") fileName: String): ResponseEntity<Any> = buildImageResponse(fileName)

    @GetMapping("/isMotionDetected")
    fun isMotionDetected(): Map<String, String> = buildResponse("motion_detected", webcamService.isMotionDetectedSinceLastCheck.toString())
}