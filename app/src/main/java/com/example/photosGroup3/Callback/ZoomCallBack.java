package com.example.photosGroup3.Callback;

import android.graphics.Bitmap;

public interface ZoomCallBack {
    public void BackToInit();
    public Bitmap RotateDegree(String currentImg,float degree,int pos);
    public void setImageView(String currentImg,int pos);


}
