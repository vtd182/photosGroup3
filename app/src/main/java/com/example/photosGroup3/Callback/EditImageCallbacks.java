package com.example.photosGroup3.Callback;

import android.graphics.Bitmap;

public interface EditImageCallbacks {
    public void TransformVertical();
    public void TransformHorizontal();

    public void BackFragment();
    public Bitmap blurFast(int radius);
    public void ConfirmBlur(Bitmap input);
    public void BitmapFilterChoose(Bitmap input,String name);

    public void recreateOnDarkMode();

}
