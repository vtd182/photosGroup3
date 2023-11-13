package com.example.photosGroup3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.photosGroup3.Callback.ISelectedPicture;
import com.example.photosGroup3.Utils.ImageDelete;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectedPicture extends AppCompatActivity implements ISelectedPicture {


    ViewPager2 viewPager2;
    ArrayList<viewPagerItem> listItem;
    String[] paths;
    String[] dates;
    String[] names;
    int[] size;
    ArrayList<String> imagesPath;
    ArrayList<String> imagesDate;
    ArrayList<Integer> imagesSize;
    MediaPlayer mediaPlayer;
    LinearLayout subInfo;
    LinearLayout changeWallpaper;
    LinearLayout changeWallpaperLock;
    LinearLayout changeFileName;


    ImageButton backBtn;
    ImageButton shareBtn;
    ImageButton infoBtn;
    ImageButton moreBtn;

    ImageButton saveBtn;


    ImageButton deleteBtn, editBtn;
    public ImageButton rotateBtn;

    String currentSelectedName = null;
    int currentPosition = -1;

    viewPagerAdapter aa = null;

    Boolean haveRotate = false;
    RelativeLayout topNav;
    RelativeLayout bottomNav;

    Bitmap rotateImage = null;
    String imageRotated = null;


    String selectedName = null;
    int lastRotate = -1;
    int totalRotate = 0;
    boolean displayNavBars = true;
    boolean displaySubBar = false;

    @SuppressLint({"ClickableViewAccessibility", "SuspiciousIndentation"})


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_picture);


        viewPager2 = findViewById(R.id.main_viewPager);


        backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(view -> SelectedPicture.super.onBackPressed());
        saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setVisibility(View.INVISIBLE);
        deleteBtn = findViewById(R.id.deleteSingleBtn);


        topNav = findViewById(R.id.topNavSinglePic);
        bottomNav = findViewById(R.id.bottomNavSinglePic);


        infoBtn = findViewById(R.id.infoBtn);

        editBtn = findViewById(R.id.editBtn);

        rotateBtn = findViewById(R.id.rotateBtn);
        subInfo = findViewById(R.id.subInfo);
        moreBtn = findViewById(R.id.moreBtn);


        topNav = findViewById(R.id.topNavSinglePic);
        bottomNav = findViewById(R.id.bottomNavSinglePic);

        //get img and name data

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            //cut name
            imagesPath = intent.getStringArrayListExtra("images");
            imagesDate = intent.getStringArrayListExtra("dates");
            imagesSize = intent.getIntegerArrayListExtra("size");
            int pos = intent.getIntExtra("pos", 0);
            String selectedName = intent.getStringExtra("name");
            ArrayList<String> images = intent.getStringArrayListExtra("images");


            assert images != null;
            names = new String[images.size()];
            // fix name from data
            for (int i = 0; i < images.size(); i++) {
                names[i] = images.get(i);
            }


            paths = new String[imagesPath.size()];
            for (int i = 0; i < imagesPath.size(); i++) {
                paths[i] = imagesPath.get(i);
            }

            dates = new String[imagesDate.size()];
            for (int i = 0; i < imagesDate.size(); i++) {
                dates[i] = imagesDate.get(i);
            }

            size = new int[imagesSize.size()];
            for (int i = 0; i < imagesSize.size(); i++) {
                size[i] = imagesSize.get(i);
            }

            listItem = new ArrayList<>();
            for (int i = 0; i < imagesPath.size(); i++) {
                viewPagerItem item = new viewPagerItem(paths[i]);
                listItem.add(item);
            }
            if (aa == null)
                aa = new viewPagerAdapter(listItem, this);

            viewPager2.setAdapter(aa);
            viewPager2.setCurrentItem(pos, false);
            viewPager2.setClipToPadding(false);
            viewPager2.setClipChildren(false);
            viewPager2.setOffscreenPageLimit(1);
            viewPager2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);


            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                    saveBtn.setVisibility(View.INVISIBLE);

                    String temp = aa.getItem(position).getSelectedName();
                    setCurrentSelectedName(aa.getItem(position).getSelectedName());
                    setCurrentPosition(position);


                    aa.BackToInit();

                }
            });

            shareBtn = findViewById(R.id.shareBtn);

        }

    }

    @Override
    public void preventSwipe() {
        viewPager2.setUserInputEnabled(false);
    }

    @Override
    public void allowSwipe() {
        viewPager2.setUserInputEnabled(true);
    }


    @Override
    public void setCurrentSelectedName(String name) {
        this.currentSelectedName = name;
    }

    @Override
    public void setCurrentPosition(int pos) {
        this.currentPosition = pos;
    }

    @Override
    public void removeImageUpdate(String input) {

    }


    @Override
    public void showNav() {

        if (!displayNavBars) {
            topNav.setVisibility(View.VISIBLE);
            bottomNav.setVisibility(View.VISIBLE);
            displayNavBars = true;
        } else {
            displayNavBars = false;
            displaySubBar = false;
            bottomNav.setVisibility(View.INVISIBLE);
            topNav.setVisibility(View.INVISIBLE);
            subInfo.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void hiddenNav() {
    }



    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void notifyChanged() {
        aa.notifyDataSetChanged();
    }


}
