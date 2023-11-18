package com.example.photosGroup3;

import java.util.ArrayList;

public class Album {
    String path;
    String name;
    ArrayList<String> imagePaths;

    public Album(String path, String name, ArrayList<String> imagePaths) {
        this.path = path;
        this.name = name;
        this.imagePaths = imagePaths;
    }
}
