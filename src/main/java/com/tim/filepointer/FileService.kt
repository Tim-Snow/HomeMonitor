package com.tim.filepointer

import com.tim.filepointer.GlobalValues.MAX_IMAGES
import org.springframework.stereotype.Component
import java.util.*

@Component
class FileService {

    val imageNames: Deque<String> = ArrayDeque()
    var totalImages: Int = 0

    init {
        cleanOldFiles()
    }

    fun getLatestImageName(): String {
        if(!imageNames.isEmpty())
            return imageNames.peekFirst()
        return "0"
    }

    fun addToImageNames(imageName: String) {
        if(imageNames.size >= MAX_IMAGES || totalImages >= MAX_IMAGES)
            deleteOldestImage()

        storeImage(imageName)
    }

    private fun storeImage(imageName: String) {
        imageNames.push(imageName)
        println("New image: " + imageName)
        totalImages++
    }

    private fun cleanOldFiles() {
        for(i in 0..totalImages){
            deleteOldestImage()
        }
    }

    private fun deleteOldestImage() {
        if(totalImages > 0) {
            println("Deleting: " + imageNames.peekLast())
            imageNames.removeLast()
            totalImages--
            //TODO Actually delete on disk
        }
    }
}