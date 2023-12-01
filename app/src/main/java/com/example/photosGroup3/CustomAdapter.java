package com.example.photosGroup3;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    private final ImageDisplay imageDisplay;
    private final ArrayList<String> imagePhotos;
    private final LayoutInflater layoutInflater;

    private class ViewHolder {
        ImageView imageView;
        CheckBox check;
    }

    public CustomAdapter(ImageDisplay imageDisplay, ArrayList<String> imagePhotos, @NonNull Context context) {
        this.imageDisplay = imageDisplay;
        this.imagePhotos = imagePhotos;

        this.layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return imagePhotos.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.row_item, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = view.findViewById(R.id.imageView);
            viewHolder.check = view.findViewById(R.id.checkImage);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (imageDisplay.isHolding) {
            viewHolder.check.setVisibility(View.VISIBLE);

            viewHolder.check.setChecked(imageDisplay.selectedImages.contains(imagePhotos.get(i)));

            viewHolder.check.setOnCheckedChangeListener((compoundButton, b) -> {
                if (compoundButton.isPressed()) {
                    if (i < imagePhotos.size()) {

                        if (b) {
                            if (!imageDisplay.selectedImages.contains(imagePhotos.get(i))) {
                                imageDisplay.selectedImages = ((MainActivity) imageDisplay.requireContext()).
                                        adjustChooseToDeleteInList(imagePhotos.get(i),
                                                "choose");
                            }

                        } else {
                            if (imageDisplay.selectedImages.contains(imagePhotos.get(i))) {
                                imageDisplay.selectedImages = ((MainActivity) imageDisplay.requireContext()).
                                        adjustChooseToDeleteInList(imagePhotos.get(i),
                                                "unchoose");
                            }
                        }
                        ((MainActivity) imageDisplay.requireContext()).SelectedTextChange();
                        imageDisplay.notifyChangeGridLayout();

                    }
                }

            });
        } else {
            viewHolder.check.setVisibility(View.INVISIBLE);
        }
        File imgFile = new File(imagePhotos.get(i));
        Glide.with(imageDisplay.context)
                .load(Uri.parse("file://" + imgFile.getAbsolutePath()))
                .into(viewHolder.imageView);
        return view;
    }
}
