package com.example.photosGroup3.Callback;

public interface ISelectedPicture {
    public void preventSwipe();
    public void allowSwipe();

    public void setCurrentSelectedName(String name);
    public void setCurrentPosition(int pos);

    public void removeImageUpdate(String input);

    public void showNav();
    public void hiddenNav();

    public void notifyChanged();


}
