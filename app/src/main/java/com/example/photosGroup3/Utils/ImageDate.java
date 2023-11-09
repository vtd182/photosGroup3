package com.example.photosGroup3.Utils;

import java.util.Date;

public class ImageDate implements Comparable<ImageDate>{
    private String image;
    private Date date;

    public ImageDate(String image, Date date) {
        this.image = image;
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String dayToString(){
        String temp = this.date.toString();
        return temp;
    }
    @Override
    public int compareTo(ImageDate imageDate) {
        return this.date.compareTo(imageDate.date);
    }


}
