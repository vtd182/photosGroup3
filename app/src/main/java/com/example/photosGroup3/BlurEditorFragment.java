package com.example.photosGroup3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BlurEditorFragment extends Fragment {
    @SuppressLint("StaticFieldLeak")
    static BlurEditorFragment instance = null;
    EditImage main;
    Context context = null;

    ImageButton backBtn, confirmBtn;
    SeekBar seekBar;
    TextView text;
    Bitmap blurredImage;

    public static BlurEditorFragment newInstance() {
        if (instance == null) {
            instance = new BlurEditorFragment();
        }
        return new BlurEditorFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            context = getContext(); // use this reference to invoke main callbacks
            main = (EditImage) getActivity();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("MainActivity must implement callbacks");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.blur_fragment, null);

        seekBar = layout.findViewById(R.id.edit_blur_amount);
        backBtn = layout.findViewById(R.id.blur_back_btns);
        backBtn.setOnClickListener(view -> main.BackFragment());
        confirmBtn = layout.findViewById(R.id.blur_confirm_btns);
        confirmBtn.setOnClickListener(view -> {
            main.ConfirmBlur(blurredImage);
            main.BackFragment();
        });
        seekBar.setMax(100);
        seekBar.setProgress(0);
        text = layout.findViewById(R.id.edit_blur_amount_num);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                text.setText("" + i);
                blurredImage = main.blurFast(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return layout;
    }

}
