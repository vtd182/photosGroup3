package com.example.photosgroup3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    String currentDirectory = null;

    String SD;
    String DCIM;
    String Picture;
    ArrayList<String> folderPaths = new ArrayList<>();
    public ArrayList<String> FileInPaths = new ArrayList<>();
    static HashMap<Long, Bitmap> hashMap = new HashMap<>();

    LinearLayout navbar;
    RelativeLayout chooseNavbar;
    RelativeLayout status;

    MainActivity context;
    FloatingActionButton deleteBtn;
    FloatingActionButton cancelBtn;
    FloatingActionButton selectAll;
    TextView informationSelected;

    FloatingActionButton createSliderBtn;
    FloatingActionButton shareMultipleBtn;
    FloatingActionButton addToAlbumBtn;
    FloatingActionButton addToFavoriteBtn;

    public static String[] ImageExtensions = new String[]{
            ".jpg",
            ".png",
            ".gif",
            ".jpeg"
    };
    LinearLayout[] arrNavLinearLayouts = new LinearLayout[3];
    ImageView[] arrNavImageViews = new ImageView[3];
    TextView[] arrNavTextViews = new TextView[3];
    private int selectedTab = 0;
    int[] arrRoundLayout = new int[3];
    int[] arrIcon = new int[3];
    int[] arrSelectedIcon = new int[3];

    Fragment[] arrFrag = new Fragment[3];


    public void askForPermissions() {
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        }
    }

    String deleteNotify = "";

    public ArrayList<String> chooseToDeleteInList = new ArrayList<>();


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    boolean isDark;
    SharedPreferences shareConfig;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}