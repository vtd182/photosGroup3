package com.example.photosGroup3.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.photosGroup3.EditImage;
import com.example.photosGroup3.R;

public class EditTransformFragment extends Fragment {
    EditImage main;
    Context context = null;
    ImageButton verticalBtn;
    ImageButton horizontalBtn;
    ImageButton backBtn;


    public static EditTransformFragment newInstance() {
        return new EditTransformFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            context = getContext(); // use this reference to invoke the onAttachMethod
            main = (EditImage) getActivity();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("MainActivity must implement callbacks");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View layout = inflater.
                inflate(R.layout.edit_transform_fragment, null);

        verticalBtn = layout.findViewById(R.id.vertical_flip);
        verticalBtn.setOnClickListener(view -> main.TransformVertical());

        horizontalBtn = layout.findViewById(R.id.horizontal_flip);
        horizontalBtn.setOnClickListener(view -> main.TransformHorizontal());

        backBtn = layout.findViewById(R.id.back_edit_btn);
        backBtn.setOnClickListener(view -> main.BackFragment());

        return layout;
    }
}
