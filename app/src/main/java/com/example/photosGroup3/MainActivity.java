package com.example.photosGroup3;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.example.photosGroup3.Callback.MainCallBack;
import com.example.photosGroup3.Utils.ImageDelete;
import com.example.photosGroup3.Utils.SlideShow;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements MainCallBack, View.OnClickListener {

    public ArrayList<String> FileInPaths = new ArrayList<>();
    static HashMap<Long, Bitmap> hashMap = new HashMap<>();
    String currentDirectory = null;
    String SD;
    String DCIM;
    String Picture;
    ArrayList<String> folderPaths = new ArrayList<>();
    LinearLayout navbar;
    RelativeLayout chooseNavbar;
    RelativeLayout status;
    MainActivity mainActivity;
    FloatingActionButton deleteBtn;
    FloatingActionButton cancelBtn;
    FloatingActionButton selectAll;
    FloatingActionButton createSliderBtn;
    FloatingActionButton shareMultipleBtn;
    FloatingActionButton addToAlbumBtn;
    FloatingActionButton addToFavoriteBtn;
    TextView informationSelected;
    String deleteNotify = "";
    boolean isDark;
    SharedPreferences shareConfig;
    SharedPreferences.Editor edit;
    public ArrayList<String> chooseToDeleteInList = new ArrayList<>();
    LinearLayout[] arrNavLinearLayouts = new LinearLayout[3];
    ImageView[] arrNavImageViews = new ImageView[3];
    TextView[] arrNavTextViews = new TextView[3];
    private int selectedTab = 0;
    int[] arrRoundLayout = new int[3];
    int[] arrIcon = new int[3];
    int[] arrSelectedIcon = new int[3];
    Fragment[] arrFrag = new Fragment[3];

    Toolbar toolbar;
    public static String[] ImageExtensions = new String[]{
            ".jpg",
            ".png",
            ".gif",
            ".jpeg"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        shareConfig = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        edit = shareConfig.edit();
        isDark = shareConfig.getBoolean("darkmode", false);

        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        edit.putBoolean("darkmode", isDark);
        edit.commit();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FileInPaths.clear();
        hashMap.clear();

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .delayBeforeLoading(0)
                .resetViewBeforeLoading(true)
                .showImageOnLoading(R.drawable.placehoder)
                .showImageForEmptyUri(R.drawable.error_image)
                .showImageOnFail(R.drawable.error_image)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);


        mainActivity = this;

        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET
                }, 1);


        DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        Picture = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        arrFrag[1] = AlbumsFragment.getInstance();
        arrFrag[2] = new SettingsFragment();

        initView();

        deleteBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        selectAll.setOnClickListener(this);
        createSliderBtn.setOnClickListener(this);
        shareMultipleBtn.setOnClickListener(this);
        addToAlbumBtn.setOnClickListener(this);
        addToFavoriteBtn.setOnClickListener(this);

        arrNavLinearLayouts[0].setOnClickListener(new NavLinearLayouts(0));
        arrNavLinearLayouts[1].setOnClickListener(new NavLinearLayouts(1));
        arrNavLinearLayouts[2].setOnClickListener(new NavLinearLayouts(2));

        setCurrentDirectory(Picture);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        Toast.makeText(this, "onCreateOptionsMenu", Toast.LENGTH_SHORT).show();
        Log.d("Callback", "Callback is called");
        getMenuInflater().inflate(R.menu.menu_for_main_activity, menu);
        return true;
    }


    @SuppressLint("SetWorldReadable")
    @Override
    public void shareImages(ArrayList<String> paths) {
        Intent intent;
        ArrayList<Bitmap> bitmaps = new ArrayList<>();

        for (int i = 0; i < paths.size(); i++) {
            bitmaps.add(BitmapFactory.decodeFile(paths.get(i)));
        }
        try {
            ArrayList<Uri> uris = new ArrayList<>();

            for (int i = 0; i < paths.size(); i++) {
                File file = new File(paths.get(i));
                FileOutputStream fOut = new FileOutputStream(file);
                bitmaps.get(i).compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();
                //noinspection ResultOfMethodCallIgnored
                file.setReadable(true, false);

                Uri uri = FileProvider.getUriForFile(this,
                        "com.example.photosGroup3.provider", file);
                uris.add(uri);
            }
            if (paths.size() == 1) {
                intent = new Intent(Intent.ACTION_SEND);
            } else {
                intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (paths.size() == 1) {
                intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            } else {
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");
            startActivity(Intent.createChooser(intent, "Share file via"));

        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean getIsDark() {
        return isDark;
    }

    @Override
    public void setIsDark(boolean status) {
        isDark = status;
        edit.putBoolean("darkmode", isDark);
        edit.commit();
        this.recreate();
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private void showSliderDialogBox() {
        final Dialog customDialog = new Dialog(mainActivity);
        customDialog.setContentView(R.layout.slider_diaglog_notify);
        Objects.requireNonNull(customDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));

        customDialog.findViewById(R.id.cancelSlider)
                .setOnClickListener(view -> customDialog.dismiss());

        customDialog.findViewById(R.id.comfirmSlider)
                .setOnClickListener(view -> {

                    RadioGroup radio = customDialog.findViewById(R.id.musicGroup);

                    int id = radio.getCheckedRadioButtonId();
                    RadioButton selectedRadionBtn = customDialog.findViewById(id);
                    String name = selectedRadionBtn.getText().toString();

                    customDialog.dismiss();

                    String[] select = chooseToDeleteInList.toArray
                            (new String[0]);

                    Intent intent = new Intent(mainActivity, SlideShow.class)
                            .putExtra("images", select)
                            .putExtra("music", name);

                    startActivity(intent);
                });
        customDialog.show();

    }


    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void showCustomDialogBox() {
        final Dialog customDialog = new Dialog(mainActivity);
        customDialog.setTitle("Delete confirm");

        customDialog.setContentView(R.layout.delete_image_confirm_dialog);
        Objects.requireNonNull(customDialog.getWindow()).
                setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));

        ((TextView) customDialog.findViewById(R.id.deleteNotify))
                .setText("Do you want to delete " + deleteNotify + " image(s) permanently in your device ?");

        customDialog.findViewById(R.id.cancel_delete)
                .setOnClickListener(view -> customDialog.dismiss());

        customDialog.findViewById(R.id.confirmDelete)
                .setOnClickListener(view -> {
                    ImageDisplay ic = ImageDisplay.getInstance();
                    String[] select = chooseToDeleteInList.toArray
                            (new String[0]);


                    removeImageUpdate(select);
                    ImageDelete.DeleteImage(select);
                    clearChooseToDeleteInList();
                    ic.deleteClicked();
                    customDialog.dismiss();
                });

        customDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                askForPermissions();

                readFolder(Picture);
                readFolder(DCIM);

                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_container, ImageDisplay.newInstance(), null)
                        .commit();

            } else {
                finish();

            }
        }
    }

    @Override
    public void setCurrentDirectory(String Dir) {
        currentDirectory = Dir;
        folderPaths.add(Dir);
    }

    @Override
    public void removeImageUpdate(String[] input) {
        for (String name : input) {

            Bitmap test = BitmapFactory.decodeFile(name);
            FileInPaths.remove(name);
            ImageDisplay.getInstance().removeImage(name);
            if (test == null) return;
            long HashCode = ImageDelete.hashBitmap(test);
            hashMap.remove(HashCode);

        }

    }

    @Override
    public void removeImageUpdate(String input) {
        FileInPaths.remove(input);
    }

    @Override
    public void renameImageUpdate(String oldName, String newName) {
        changeFileInFolder(Picture, oldName, newName);
        changeFileInFolder(DCIM, oldName, newName);
    }

    @Override
    public void addImageUpdate(String[] input) {
        for (String name : input) {
            filterImage(name);

        }

    }

    @Override
    public void Holding(boolean isHolding) {
        ImageDisplay instance = ImageDisplay.getInstance();
        if (isHolding) {
            chooseNavbar.setVisibility(View.VISIBLE);
            navbar.setVisibility(View.INVISIBLE);
            status.setVisibility(View.VISIBLE);
            if (instance.callback != null) {
                instance.callback.onLongClick();
            }
        } else {
            chooseNavbar.setVisibility(View.INVISIBLE);
            navbar.setVisibility(View.VISIBLE);
            status.setVisibility(View.INVISIBLE);

            if (instance.callback != null) {
                instance.callback.afterLongClick();
            }


        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void SelectedTextChange() {

        deleteNotify = chooseToDeleteInList.size() + "";
        informationSelected.setText(chooseToDeleteInList.size() + " images selected");
    }

    @Override
    public ArrayList<String> chooseToDeleteInList() {
        return chooseToDeleteInList;
    }

    @Override
    public void clearChooseToDeleteInList() {
        chooseToDeleteInList.clear();
    }

    @Override
    public ArrayList<String> adjustChooseToDeleteInList(String ListInp, String type) {
        switch (type) {
            case "choose":

                if (!chooseToDeleteInList.contains(ListInp)) {
                    chooseToDeleteInList.add(ListInp);
                }

                break;
            case "unchoose":

                chooseToDeleteInList.remove(ListInp);

                break;

        }
        return chooseToDeleteInList;
    }


    public void filterImage(String name) {
        Bitmap test = BitmapFactory.decodeFile(name);
        if (test == null) return;
        long HashCode = ImageDelete.hashBitmap(test);
        if (!hashMap.containsKey(HashCode)) {
            FileInPaths.add(name);
            hashMap.put(HashCode, test);
        } else {
            ImageDelete.DeleteImage(name);
        }
    }

    private void readFolder(String Dir) {


        File sdFile = new File(Dir);

        File[] foldersSD = sdFile.listFiles();

        try {
            assert foldersSD != null;
            for (File file : foldersSD) {
                if (file.isDirectory()) {
                    if (file.getName().equals("Albums")) {
                        continue;
                    }
                    //get absolute
                    //do nothing
                    readFolder(file.getAbsolutePath());

                } else {
                    for (String extension : ImageExtensions) {
                        if (file.getAbsolutePath().toLowerCase().endsWith(extension)) {
                            if (!FileInPaths.contains(file.getAbsolutePath()))
                                filterImage(file.getAbsolutePath());

                            break;
                        }

                    }
                }
            }

        } catch (Exception ignored) {
        }

    }

    public static boolean checkInHash(String name) {
        Bitmap test = BitmapFactory.decodeFile(name);

        if (test == null) return false;
        // boolean have= false;
        long HashCode = ImageDelete.hashBitmap(test);
        if (!hashMap.containsKey(HashCode)) {

            hashMap.put(HashCode, test);
            return true;
        } else {
            ImageDelete.DeleteImage(name);
        }
        return false;

    }

    @Override
    public void removeInHash(String name) {
        new File(name);

        Bitmap test = BitmapFactory.decodeFile(name);

        if (test == null) return;
        // boolean have= false;
        long HashCode = ImageDelete.hashBitmap(test);
        hashMap.remove(HashCode);


    }


    private void changeFileInFolder(String Dir, String oldName, String newName) {

        File sdFile = new File(Dir);
        File[] foldersSD = sdFile.listFiles();

        try {
            assert foldersSD != null;
            for (File file : foldersSD) {
                if (file.isDirectory()) {
                    if (file.getName().equals("Albums")) {
                        continue;
                    }
                    //get absolute
                    //do nothing
                    changeFileInFolder(file.getAbsolutePath(), oldName, newName);

                } else {
                    for (String extension : ImageExtensions) {

                        if (file.getAbsolutePath().toLowerCase().endsWith(extension)) {
                            //      filterImage();
                            if (oldName.equals(file.getName())) {
                                File from = new File(Dir + "/" + oldName);
                                File to = new File(Dir + "/" + newName);
                                //noinspection ResultOfMethodCallIgnored
                                from.renameTo(to);
                                FileInPaths.remove(Dir + "/" + oldName);
                                FileInPaths.add(Dir + "/" + newName);
                                Toast.makeText(this, "doi xong", Toast.LENGTH_SHORT).show();

                                ImageDisplay ic = ImageDisplay.getInstance();
                                ic.notifyChangeGridLayout();
                                break;

                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            //do nothing
        }

    }


    @Override
    public String getSDDirectory() {
        return SD;
    }

    @Override
    public String getCurrentDirectory() {
        return currentDirectory;
    }

    @Override
    public void pushFolderPath(String inp) {
        folderPaths.add(inp);
    }

    @Override
    public void popFolderPath() {
        folderPaths.remove(folderPaths.size() - 1);
        currentDirectory = folderPaths.get(folderPaths.size() - 1);
    }

    @Override
    public void readAgain() {

        readFolder(Picture);
        readFolder(DCIM);
    }

    @Override
    public ArrayList<String> getFolderPath() {
        return folderPaths;
    }

    @Override
    public String getDCIMDirectory() {
        return DCIM;
    }

    @Override
    public String getPictureDirectory() {
        return Picture;
    }

    @Override
    public ArrayList<String> getFileinDir() {
        return FileInPaths;
    }



    public void askForPermissions() {
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        }
    }

    void initView() {
        arrRoundLayout[0] = R.drawable.round_photos;
        arrRoundLayout[1] = R.drawable.round_albums;
        arrRoundLayout[2] = R.drawable.round_settings;

        navbar = findViewById(R.id.navbar);
        chooseNavbar = findViewById(R.id.selectNavbar);
        status = findViewById(R.id.status);

        deleteBtn = findViewById(R.id.deleteImageButton);
        cancelBtn = findViewById(R.id.clear);
        selectAll = findViewById(R.id.selectAll);
        createSliderBtn = findViewById(R.id.createSliderBtn);
        shareMultipleBtn = findViewById(R.id.shareMultipleBtn);
        addToAlbumBtn = findViewById(R.id.addToAlbumBtn);
        addToFavoriteBtn = findViewById(R.id.addToFavoriteBtn);
        informationSelected = findViewById(R.id.infomationText);


        arrIcon[0] = R.drawable.ic_baseline_photo;
        arrIcon[1] = R.drawable.ic_baseline_photo_library;
        arrIcon[2] = R.drawable.ic_baseline_settings;


        arrSelectedIcon[0] = R.drawable.ic_baseline_photo_selected;
        arrSelectedIcon[1] = R.drawable.ic_baseline_photo_library_selected;
        arrSelectedIcon[2] = R.drawable.ic_baseline_settings_selected;

        arrNavLinearLayouts[0] = findViewById(R.id.photosLayout);
        arrNavLinearLayouts[1] = findViewById(R.id.albumsLayout);
        arrNavLinearLayouts[2] = findViewById(R.id.settingsLayout);

        arrNavImageViews[0] = findViewById(R.id.photos_img);
        arrNavImageViews[1] = findViewById(R.id.albums_img);
        arrNavImageViews[2] = findViewById(R.id.settings_img);

        arrNavTextViews[0] = findViewById(R.id.photos_txt);
        arrNavTextViews[1] = findViewById(R.id.albums_txt);
        arrNavTextViews[2] = findViewById(R.id.settings_txt);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.deleteImageButton) {
            showCustomDialogBox();
        } else if (viewId == R.id.clear) {
            ImageDisplay ic = ImageDisplay.getInstance();
            clearChooseToDeleteInList();
            ic.clearClicked();
        } else if (viewId == R.id.selectAll) {
            ImageDisplay ic2 = ImageDisplay.getInstance();
            if (chooseToDeleteInList.size() == ic2.images.size()) {
                chooseToDeleteInList.clear();
            } else {
                chooseToDeleteInList = new ArrayList<>(ic2.images);
            }
            ic2.selectAllClicked();
        } else if (viewId == R.id.createSliderBtn) {
            Toast.makeText(mainActivity, "Create slideshow", Toast.LENGTH_SHORT).show();
            showSliderDialogBox();
        } else if (viewId == R.id.shareMultipleBtn) {
            Toast.makeText(mainActivity, "Share multiple", Toast.LENGTH_SHORT).show();
            String[] select = chooseToDeleteInList.toArray(new String[0]);
            ArrayList<String> paths = new ArrayList<>();
            Collections.addAll(paths, select);
            shareImages(paths);
        } else if (viewId == R.id.addToAlbumBtn) {
            AlbumChoosingDialog albumChoosingDialog = new AlbumChoosingDialog(mainActivity);
            albumChoosingDialog.show();
        } else if (viewId == R.id.addToFavoriteBtn) {
            MoveOrCopyForDialog moveOrCopyForDialogDialog = new MoveOrCopyForDialog(mainActivity, new MoveOrCopyForDialog.MoveOrCopyCallBack() {
                @Override
                public void dismissCallback(String method) {
                    if (method.equals("remove")) {
                        ImageDisplay ic = ImageDisplay.getInstance();
                        clearChooseToDeleteInList();
                        ic.clearClicked();
                    }
                }

                @Override
                public void copiedCallback(String newImagePath) {
                    assert AlbumsFragment.favoriteAlbum() != null;
                    AlbumsFragment.favoriteAlbum().imagePaths.add(newImagePath);
                }

                @Override
                public void removedCallback(String oldImagePath, String newImagePath) {
                    ImageDisplay.getInstance().removeImage(oldImagePath);
                    assert AlbumsFragment.favoriteAlbum() != null;
                    AlbumsFragment.favoriteAlbum().imagePaths.add(newImagePath);
                }
            }, AlbumsFragment.favoriteAlbum(), chooseToDeleteInList());
            moveOrCopyForDialogDialog.show();
        }
    }

    protected class NavLinearLayouts implements View.OnClickListener {
        public int thisIndex;

        NavLinearLayouts(int index) {
            thisIndex = index;
        }


        @SuppressLint("ResourceAsColor")
        @Override
        public void onClick(View view) {
            if (selectedTab != thisIndex) {
                selectedTab = thisIndex;

                if (thisIndex != 0) {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragment_container, arrFrag[thisIndex], null)
                            .commit();
                } else {
                    ImageDisplay.restoreINSTANCE();
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragment_container, ImageDisplay.newInstance(), null)
                            .commit();
                }
                for (int i = 0; i < 3; i++) {
                    if (i != thisIndex) {
                        arrNavTextViews[i].setVisibility(View.GONE);

                    }
                    arrNavTextViews[thisIndex].setVisibility(View.VISIBLE);
                    ScaleAnimation scaleAnimation = new ScaleAnimation(0.8f,
                            1.0f, 1f, 1f,
                            Animation.RELATIVE_TO_SELF, 0,
                            Animation.RELATIVE_TO_SELF, 0.0f);
                    scaleAnimation.setDuration(200);
                    scaleAnimation.setFillAfter(true);
                    arrNavLinearLayouts[thisIndex].startAnimation(scaleAnimation);
                }


            }
        }

    }

    private class AlbumChoosingDialog extends Dialog {
        @SuppressLint("UseCompatLoadingForDrawables")
        public AlbumChoosingDialog(@NonNull Context context) {
            super(context);
            @SuppressLint("InflateParams") View layout =
                    getLayoutInflater().inflate(R.layout.album_choosing, null);

            ImageButton backBtn = layout.findViewById(R.id.album_choosing_back);
            backBtn.setOnClickListener(view -> dismiss());

            GridView imageList = layout.findViewById(R.id.album_choosing_list);
            imageList.setAdapter(new AlbumChoosingAdapter());

            imageList.setOnItemClickListener((adapterView, view, i, l) -> {
                view.setBackgroundResource(R.drawable.custom_row_album);

                Drawable buttonDrawable = view.getBackground();
                buttonDrawable = DrawableCompat.wrap(buttonDrawable);
                DrawableCompat.setTint(buttonDrawable, context.getResources().getColor(R.color.fullScreenBtn));
                view.setBackground(buttonDrawable);
                MoveOrCopyForDialog dialog = new MoveOrCopyForDialog(context, new MoveOrCopyForDialog.MoveOrCopyCallBack() {
                    @Override
                    public void dismissCallback(String method) {
                        view.setBackgroundResource(android.R.color.transparent);

                        view.setBackgroundTintList(null);
                        TextView imgCount = view.findViewById(R.id.album_images_count);
                        imgCount.setText(String.format(context.getString(R.string.album_image_count), AlbumsFragment.albumList.get(i).imagePaths.size()));
                        if (method.equals("remove")) {
                            ImageDisplay ic = ImageDisplay.getInstance();
                            clearChooseToDeleteInList();
                            ic.clearClicked();
                            dismiss();
                        }
                    }

                    @Override
                    public void copiedCallback(String newImagePath) {
                        AlbumsFragment.albumList.get(i).imagePaths.add(newImagePath);
                    }

                    @Override
                    public void removedCallback(String oldImagePath, String newImagePath) {
                        ImageDisplay.getInstance().removeImage(oldImagePath);
                        AlbumsFragment.albumList.get(i).imagePaths.add(newImagePath);
                    }


                }, AlbumsFragment.albumList.get(i), chooseToDeleteInList());
                dialog.show();
            });

            setContentView(layout);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(Objects.requireNonNull(getWindow()).getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
            getWindow().setAttributes(layoutParams);
        }

        private class AlbumChoosingAdapter extends BaseAdapter {
            ArrayList<Album> albumList;

            public AlbumChoosingAdapter() {
                this.albumList = AlbumsFragment.albumList;
            }

            @Override
            public int getCount() {
                return albumList.size();
            }

            @Override
            public Object getItem(int i) {
                return albumList.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                ViewHolder viewHolder;
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.row_album, viewGroup, false);
                    viewHolder = new ViewHolder();
                    viewHolder.albumName = view.findViewById(R.id.album_name);
                    viewHolder.albumImageCount = view.findViewById(R.id.album_images_count);
                    viewHolder.imageView = view.findViewById(R.id.album_image);
                    view.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) view.getTag();
                }
                viewHolder.albumName.setText(albumList.get(i).name);
                viewHolder.albumImageCount.setText(String.format(mainActivity.getString(R.string.album_image_count), albumList.get(i).imagePaths.size()));
                view.setBackgroundTintList(null);
                if (albumList.get(i).name.equals(AlbumsFragment.favourite)) {
                    viewHolder.imageView.setImageResource(R.drawable.ic_baseline_favorite_24);
                } else {
                    viewHolder.imageView.setImageResource(R.drawable.ic_baseline_folder_24);
                }
                return view;
            }
            private class ViewHolder {
                TextView albumName;
                TextView albumImageCount;
                ImageView imageView;
            }
        }
    }
}