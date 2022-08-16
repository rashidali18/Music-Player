package com.example.mymusicplayer;

public class AudioData {
    private String name;
    private String path;
    private String album;

    public AudioData() {
    }

    public AudioData(String name, String path, String artist) {
        this.name = name;
        this.path = path;
        this.album = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
