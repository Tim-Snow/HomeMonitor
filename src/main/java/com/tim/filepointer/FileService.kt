package com.tim.filepointer

import com.tim.filepointer.GlobalValues.MAX_IMAGES
import org.springframework.stereotype.Component

@Component
class FileService {

    var totalImages: Int = 0

    val imageNames = Array<String>(5) { "it = $it" }

    init {
        cleanOldFiles()
    }

    fun getLatestImageName(): String {
        if(!imageNames.isEmpty())
            return imageNames[totalImages]
        return "0"
    }

    fun addToImageNames(imageName: String) {
        if(imageNames.size >= MAX_IMAGES || totalImages >= MAX_IMAGES)
            deleteImage(0)

        storeImage(imageName)
    }

    private fun storeImage(imageName: String) {
        imageNames[totalImages + 1] = imageName
        totalImages++
    }

    private fun cleanOldFiles() { }

    private fun deleteImage(index: Int) {
        for((index, value) in imageNames.withIndex()){
            
        }

        imageNames[index] = ""
        totalImages--
    }
}