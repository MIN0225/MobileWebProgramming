package com.example.gallery;

import java.util.ArrayList;

// Album class
public class Album {
    private String name;
    private ArrayList<String> images;

    public Album(String name) {
        this.name = name;
        this.images = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void addImage(String imagePath) {
        this.images.add(imagePath);
    }

    public void removeImage(String imagePath) {
        this.images.remove(imagePath);
    }
}

