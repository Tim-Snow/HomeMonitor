package com.tim.filepointer

import com.tim.filepointer.GlobalValues.WEBCAM_ENABLED
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.nio.file.Files

@RestController
class FilePointerController @Autowired constructor(private val fileService: FileService, private val webcamService: WebcamService) {

    @GetMapping("/total_images")
    fun totalImages(): Map<String, String> {
        return Util.buildResponse("total_images", fileService.totalImages.toString())
    }

    @GetMapping("/name/all_images")
    fun allImageNames(): Map<String, String> {
        return Util.buildMultiResponse("file", fileService.allImageNames)
    }

    @GetMapping("/name/latest")
    fun latestImageName(): Map<String, String> {
        return Util.buildResponse("file", fileService.latestImageName)
    }

    @GetMapping(value = "/image/manual_capture")
    fun manualCaptureImage(): ResponseEntity<Any> {
        if(WEBCAM_ENABLED) {
            val file = File(Util.fileNameBuilder(webcamService.manualCapture()))
            val fileByte: ByteArray = Files.readAllBytes(file.toPath())
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(fileByte)
        } else {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(Util.buildResponse("error", "Webcam not enabled."))
        }
    }

    @GetMapping(value = "/image/latest")
    fun latestImage(): ResponseEntity<Any> {
        if(WEBCAM_ENABLED) {
            val file = File(Util.fileNameBuilder(fileService.latestImageName))
            val fileByte: ByteArray = Files.readAllBytes(file.toPath())
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileByte)
        } else {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(Util.buildResponse("error", "Webcam not enabled."))
        }
    }

    @GetMapping(value = "/image/{fileName}")
    fun getImageByName(@PathVariable("fileName") fileName: String): ResponseEntity<Any> {
        if(WEBCAM_ENABLED){
            val file = File(Util.fileNameBuilder(fileName))
            val fileByte: ByteArray = Files.readAllBytes(file.toPath())
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileByte)
        } else {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(Util.buildResponse("error", "Webcam not enabled."))
        }
    }
}