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
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectedPicture extends AppCompatActivity implements ISelectedPicture {


    ViewPager2 viewPager2;
    ArrayList<viewPagerItem> listItem;
    ArrayList<String> imagesPath;
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
        saveBtn.setOnClickListener(view -> {
            if (rotateImage != null && imageRotated != null) {

                String newImgPath = ImageDelete.saveImage(rotateImage, imageRotated);
                ImageDisplay.getInstance().addNewImage(newImgPath, 0);
                Intent intent = new Intent();
                setResult(2, intent);
                finish();

            }

            ImageDisplay.getInstance().notifyChangeGridLayout();
            rotateImage = null;
            imageRotated = null;
            haveRotate = false;
            saveBtn.setVisibility(View.INVISIBLE);

        });
        saveBtn.setVisibility(View.INVISIBLE);
        deleteBtn = findViewById(R.id.deleteSingleBtn);
        deleteBtn.setOnClickListener(view -> showCustomDialogBoxDelete());


        topNav = findViewById(R.id.topNavSinglePic);
        bottomNav = findViewById(R.id.bottomNavSinglePic);


        infoBtn = findViewById(R.id.infoBtn);
        infoBtn.setOnClickListener(view -> showCustomDialogBoxInformation());

        editBtn = findViewById(R.id.editBtn);
        editBtn.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(), "edit", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), EditImage.class);

            intent.putExtra("imgPath", currentSelectedName);
            someActivityResultLauncher.launch(intent);

        });
        rotateBtn = findViewById(R.id.rotateBtn);
        rotateBtn.setOnClickListener(view -> {

            haveRotate = true;
            imageRotated = currentSelectedName;
            saveBtn.setVisibility(View.VISIBLE);
            rotateImage = aa.RotateDegree(currentSelectedName, 90, currentPosition);

            selectedName = currentSelectedName;
            lastRotate = currentPosition;
            totalRotate += 90;
        });
        subInfo = findViewById(R.id.subInfo);
        moreBtn = findViewById(R.id.moreBtn);
        moreBtn.setOnClickListener(view -> {
            if (displaySubBar) {
                subInfo.setVisibility(View.INVISIBLE);
                displaySubBar = false;
            } else {
                subInfo.setVisibility(View.VISIBLE);
                displaySubBar = true;
            }
        });


        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        changeWallpaper = findViewById(R.id.changeWallpaper);
        changeWallpaperLock = findViewById(R.id.changeWallpaperLock);
        changeFileName = findViewById(R.id.changeNameFile);

        changeWallpaper.setOnClickListener(view -> {
            try {
                wallpaperManager.setBitmap(getItemBitmap(currentSelectedName));
                showDialogSuccessChange("Change Wallpaper Successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }
            subInfo.setVisibility(View.INVISIBLE);
        });
        changeWallpaperLock.setOnClickListener(view -> {
            try {
                wallpaperManager.setBitmap(getItemBitmap(currentSelectedName), null, false, WallpaperManager.FLAG_LOCK);
                showDialogSuccessChange("Change Lock sceen Wallpaper Successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }
            subInfo.setVisibility(View.INVISIBLE);
        });
        changeFileName.setOnClickListener(view -> {
            displaySubBar = false;
            subInfo.setVisibility(View.INVISIBLE);
            showDialogRename();
        });

        topNav = findViewById(R.id.topNavSinglePic);
        bottomNav = findViewById(R.id.bottomNavSinglePic);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            //cut name
            imagesPath = intent.getStringArrayListExtra("images");
            int pos = intent.getIntExtra("pos", 0);
            String selectedName = intent.getStringExtra("name");
            ArrayList<String> images = intent.getStringArrayListExtra("images");


            assert images != null;

            listItem = new ArrayList<>();
            for (int i = 0; i < imagesPath.size(); i++) {
                viewPagerItem item = new viewPagerItem(imagesPath.get(i));
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
                    if (haveRotate && position != currentPosition) {
                        showCustomDialogBoxInRotatePicture(rotateImage, imageRotated);

                    }
                    setCurrentSelectedName(aa.getItem(position).getSelectedName());
                    setCurrentPosition(position);


                    aa.BackToInit();

                }
            });

            shareBtn = findViewById(R.id.shareBtn);
            shareBtn.setOnClickListener(view -> {

                ArrayList<String> listPaths = new ArrayList<>();
                listPaths.add(currentSelectedName);
                shareSingleImages(listPaths);
            });

        }

    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {

                    Intent get = result.getData();
                    assert get != null;
                    String imgName = get.getStringExtra("imgPath");

                    ImageDisplay.getInstance().addNewImage(imgName, 0);
                    Intent intent = new Intent();
                    setResult(2, intent);
                    finish();

                }
            });

    @SuppressLint("SetWorldReadable")
    public void shareSingleImages(ArrayList<String> paths) {

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

                Uri uri = FileProvider.getUriForFile(getApplicationContext(),
                        "com.example.photosGroup3.provider", file);
                uris.add(uri);
            }
            Intent intent;

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

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void removeImageUpdate(String input) {
        // remove on adapter
        listItem.remove(currentPosition);
        viewPager2.setCurrentItem(currentPosition, false);
        aa.notifyDataSetChanged();

    }

    public void renameImageUpdate(String input) {
        // rename on selected image
        imagesPath.set(currentPosition, imagesPath.get(currentPosition).
                substring(0, imagesPath.get(currentPosition).
                        lastIndexOf("/") + 1) + input);
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

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void showCustomDialogBoxDelete() {
        final Dialog customDialog = new Dialog(this);
        customDialog.setTitle("Delete confirm");

        customDialog.setContentView(R.layout.delete_image_confirm_dialog);
        Objects.requireNonNull(customDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
        ((TextView) customDialog.findViewById(R.id.deleteNotify))
                .setText("Do you want to delete in your device ?");

        customDialog.findViewById(R.id.cancel_delete)
                .setOnClickListener(view -> {
                    customDialog.dismiss();
                });

        customDialog.findViewById(R.id.confirmDelete)
                .setOnClickListener(view -> {
                    ImageDisplay ic = ImageDisplay.getInstance();
                    ImageDelete.DeleteImage(currentSelectedName);
                    removeImageUpdate(currentSelectedName);
                    ic.deleteClicked(currentSelectedName);
                    customDialog.dismiss();
                    onBackPressed();

                });
        customDialog.show();
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void notifyChanged() {
        aa.notifyDataSetChanged();
    }


    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void showCustomDialogBoxInSelectedPicture() {
        final Dialog customDialog = new Dialog(this);
        customDialog.setTitle("Delete confirm");

        customDialog.setContentView(R.layout.delete_image_confirm_dialog);
        Objects.requireNonNull(customDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
        ((TextView) customDialog.findViewById(R.id.deleteNotify))
                .setText("Do you want to delete in your device ?");

        customDialog.findViewById(R.id.cancel_delete)
                .setOnClickListener(view -> customDialog.dismiss());

        customDialog.findViewById(R.id.confirmDelete)
                .setOnClickListener(view -> {
                    ImageDisplay ic = ImageDisplay.getInstance();
                    ImageDelete.DeleteImage(currentSelectedName);
                    removeImageUpdate(currentSelectedName);
                    ic.deleteClicked(currentSelectedName);
                    customDialog.dismiss();
                });
        customDialog.show();
    }


    @SuppressLint("SetTextI18n")
    private void showCustomDialogBoxInformation() {
        final Dialog customDialog = new Dialog(this);
        File imgFile = new File(imagesPath.get(currentPosition));
        Date lastModDate = new Date(imgFile.lastModified());
        customDialog.setTitle("Information of Picture");

        customDialog.setContentView(R.layout.infomation_picture_dialog);
        Objects.requireNonNull(customDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

//
        ((TextView) customDialog.findViewById(R.id.photoName))
                .setText(shortenName(ImageDisplay.getDisplayName(imagesPath.get(currentPosition))));
        ((TextView) customDialog.findViewById(R.id.photoPath))
                .setText(imagesPath.get(currentPosition));
        ((TextView) customDialog.findViewById(R.id.photoLastModified))
                .setText(lastModDate.toString());
        ((TextView) customDialog.findViewById(R.id.photoSize))
                .setText(Math.round((imgFile.getTotalSpace()) * 1.0 / 1024) + " KB");
//        Toast.makeText(this, imagesSize[currentPosition]+"", Toast.LENGTH_SHORT).show();
        customDialog.findViewById(R.id.ok_button)
                .setOnClickListener(view -> {
                    //donothing
                    customDialog.dismiss();
                });
        customDialog.show();
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void showCustomDialogBoxInRotatePicture(Bitmap rotateImage2, String imageRotated2) {
        final Dialog customDialog = new Dialog(this);
        customDialog.setTitle("Change confirm");

        customDialog.setContentView(R.layout.delete_image_confirm_dialog);
        Objects.requireNonNull(customDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
        ((TextView) customDialog.findViewById(R.id.deleteNotify))
                .setText("Do you want to save your change ?");
        ((TextView) customDialog.findViewById(R.id.titleBox))
                .setText("Change");

        customDialog.findViewById(R.id.cancel_delete)

                .setOnClickListener(view -> {
                    //donothing
                    aa.RotateDegree(selectedName, -totalRotate, lastRotate);
                    selectedName = null;
                    lastRotate = -1;
                    totalRotate = 0;
                    customDialog.dismiss();
                });


        customDialog.findViewById(R.id.confirmDelete)
                .setOnClickListener(view -> {
                    if (rotateImage2 != null && imageRotated2 != null) {

                        String[] temp = new String[1];

                        temp[0] = ImageDelete.saveImage(rotateImage2, imageRotated2);

                        ImageDisplay.getInstance().addNewImage(temp[0], 0);


                        Intent intent = new Intent();
                        setResult(2, intent);
                        finish();

                    }
                    aa.notifyItemChanged(currentPosition);
                    ImageDisplay.getInstance().notifyChangeGridLayout();

                    Toast.makeText(getApplicationContext(), "Changed", Toast.LENGTH_SHORT).show();
                    customDialog.dismiss();
                });

        haveRotate = false;
        imageRotated = null;
        rotateImage = null;
        customDialog.show();
    }

    private void showDialogSuccessChange(String message) {
        final Dialog customDialog = new Dialog(this);
        customDialog.setTitle("Message");

        customDialog.setContentView(R.layout.show_success_dialog);
        Objects.requireNonNull(customDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

//
        ((TextView) customDialog.findViewById(R.id.messageShow))
                .setText(message);

        customDialog.findViewById(R.id.ok_button)
                .setOnClickListener(view -> {
                    //donothing
                    customDialog.dismiss();
                    onBackPressed();
                });

        customDialog.show();
    }

    private void showDialogRename() {
        final Dialog customDialog = new Dialog(this);
        customDialog.setTitle("Change Wallpaper");

        customDialog.setContentView(R.layout.rename_dialog);
        Objects.requireNonNull(customDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        customDialog.findViewById(R.id.cancel)
                .setOnClickListener(view -> {
                    //thực hiện đổi tên tại đây
                    customDialog.dismiss();
                });
        customDialog.findViewById(R.id.ok_button)
                .setOnClickListener(view -> {
                    ImageDisplay ic = ImageDisplay.getInstance();
                    EditText editText = customDialog.findViewById(R.id.editChangeFileName);
                    if (!isFileName(editText.getText() + "")) {
                        customDialog.findViewById(R.id.errorName).setVisibility(View.VISIBLE);
                    } else {
                        String fileExtension = imagesPath.get(currentPosition).substring(imagesPath.get(currentPosition).lastIndexOf("."));
                        while (fileExtension.charAt(fileExtension.length() - 1) == '\n') {
                            fileExtension = fileExtension.substring(0, fileExtension.length() - 1);
                        }
                        String newName = editText.getText() + fileExtension;
                        customDialog.dismiss();
                        File oldImg = new File(imagesPath.get(currentPosition));
                        String oldImg_name = oldImg.getName();
                        File newImg = new File(imagesPath.get(currentPosition).replace(oldImg_name, newName));

                        ic.removeImage(oldImg.getAbsolutePath());
                        if (oldImg.renameTo(newImg)) {
                            newImg = new File(imagesPath.get(currentPosition).replace(oldImg_name, newName));
                            ic.addNewImage(newImg.getAbsolutePath(), 0);
                            Toast.makeText(getApplicationContext(), "Rename succeeded", Toast.LENGTH_SHORT).show();
                        } else {
                            ic.addNewImage(oldImg.getAbsolutePath(), 0);
                            Toast.makeText(getApplicationContext(), "Rename failed", Toast.LENGTH_SHORT).show();
                        }
                        renameImageUpdate(newName);
                        showDialogSuccessChange("File name change successfully !");
                    }
                });

        customDialog.show();
    }

    public boolean isFileName(String s) {
        if (s == null || s.trim().isEmpty()) {
            System.out.println("Incorrect format of string");
            return false;
        }
        Pattern p = Pattern.compile("[^A-Za-z0-9. ]");
        Matcher m = p.matcher(s);
        boolean b = m.find();
        return !b;
    }

    public void deleteStringArrayByPossision(String[] arr, int pos) {
        int size = arr.length;
        if (pos != arr.length - 1) {
            for (int i = pos; i < size - 1; i++) {
                arr[pos] = arr[pos + 1];
            }
        }
    }

    public void deleteIntergerArrayByPossision(int[] arr, int pos) {
        int size = arr.length;
        if (pos != arr.length - 1) {
            for (int i = pos; i < size - 1; i++) {
                arr[pos] = arr[pos + 1];
            }
        }
    }

    public Bitmap getItemBitmap(String selectedName) {
        File imgFile = new File(selectedName);
        return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
    }

    public String shortenName(String name) {
        String[] ArrayName = name.split("\\.");
        String displayName;

        if (ArrayName[0].length() > 25) {
            displayName = ArrayName[0].substring(0, 10);
            displayName += "...";
            displayName += ArrayName[0].substring(ArrayName[0].length() - 10);
        } else {
            displayName = ArrayName[0];
        }
        displayName += "." + ArrayName[1];
        return displayName;
    }
}
