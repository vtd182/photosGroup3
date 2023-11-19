package com.example.photosGroup3.Utils;

public class ImageName implements Comparable<ImageName>{
    private String image;
    private String name;

    public ImageName(String image, String name) {
        this.image = image;
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
            this.name = name;
    }

    @Override
    public int compareTo(ImageName imageDate) {
        return this.name.compareTo(imageDate.name);
    }


}
