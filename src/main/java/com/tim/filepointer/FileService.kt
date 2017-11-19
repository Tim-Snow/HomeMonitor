package com.tim.filepointer

import com.tim.filepointer.GlobalValues.MAX_IMAGES
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.*
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
            deleteOrStoreOldestImage()

        storeImage(imageName)
    }

    private fun storeImage(imageName: String) {
        imageNames.push(imageName)
        println("New image: " + imageName)
        totalImages++
    }

    fun cleanOldFiles() {
        for(i in 0..totalImages){
            deleteOrStoreOldestImage()
        }
    }

    private fun deleteOrStoreOldestImage() {
        if(totalImages > 0) {
            val name: String = imageNames.peekLast()
            val file = File(name)
            val sourceDirectory = File("").absolutePath + "/"
            val storageDirectory = sourceDirectory + "storage/"

            if(name.startsWith("MOTION")) {
                println("Storing: " + name)
                val source: Path = Paths.get(sourceDirectory + name)
                Files.move(source, Paths.get(storageDirectory).resolve(source.fileName), StandardCopyOption.REPLACE_EXISTING)
            } else {
                println("Deleting: " + name)
                file.delete()
            }

            imageNames.removeLast()
            totalImages--
        }
    }
}