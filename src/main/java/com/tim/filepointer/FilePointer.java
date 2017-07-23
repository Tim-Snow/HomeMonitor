package com.tim.filepointer;

public class FilePointer {

    private final long id;
    private final String content;

    FilePointer(long id, String content){
        this.id = id;
        this.content = content;
    }

    public long getId(){
        return id;
    }

    public String getContent(){
        return content;
    }
}
