package com.tim.filepointer

import com.tim.filepointer.GlobalValues.MAX_IMAGES
import org.springframework.stereotype.Component
import java.util.*

@Component
class FileService {

    var totalImages: Int = 0
    var imageNames: Stack<String> = Stack()

    fun getLatestImageName(): String {
        if(!imageNames.empty())
            return imageNames.peek()
        return "0"
    }

    fun addToImageNames(imageName: String) {
        if(imageNames.size >= MAX_IMAGES || totalImages >= MAX_IMAGES)
            deleteImage(0)

        storeImage(imageName)
    }

    private fun storeImage(imageName: String) {
        imageNames.push(imageName)
        totalImages++
    }

    private fun deleteImage(index: Int) {
        imageNames.removeAt(index)
        //TODO move all elements down
    }

    private fun cleanOldFiles() {

    }

    init {
        cleanOldFiles()
    }

}