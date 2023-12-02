package com.example.photosGroup3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class MoveOrCopyForDialog extends BottomSheetDialog {
    MoveOrCopyCallBack callBack;
    Album album;
    ArrayList<String> addedPaths;
    String method = "";

    @Override
    public void dismiss() {
        super.dismiss();
        ImageDisplay.getInstance().listAdapter.notifyDataSetChanged();
        if (callBack != null) {
            callBack.dismissCallback(method);
        }
    }

    public MoveOrCopyForDialog(@NonNull Context context, @NonNull MoveOrCopyCallBack callBack,
                               Album album, ArrayList<String> addedPath) {
        super(context);
        this.callBack = callBack;
        this.album = album;
        this.addedPaths = addedPath;

        @SuppressLint("InflateParams") View layout = getLayoutInflater().inflate(R.layout.move_or_copy_choosing, null);

        Button move_btn = layout.findViewById(R.id.move_option);
        move_btn.setOnClickListener(view -> {
            method = "remove";
            String folderPath = AlbumsFragment.folderPath + "/" + album.name;
            for (int i = 0; i < addedPaths.size(); i++) {
                String newFileName = moveFile(addedPaths.get(i), folderPath);
                callBack.removedCallback(addedPath.get(i), folderPath + "/" + newFileName);
            }

            dismiss();
        });

        Button copy_btn = layout.findViewById(R.id.copy_option);
        copy_btn.setOnClickListener(view -> {
            method = "copy";
            String folderPath = AlbumsFragment.folderPath + "/" + album.name;
            for (int i = 0; i < addedPaths.size(); i++) {
                String newFileName = copyFile(addedPaths.get(i), folderPath);
                callBack.copiedCallback(folderPath + "/" + newFileName);
            }

            dismiss();
        });

        if (album.name.equals(AlbumsFragment.privateAlbum)){
            copy_btn.setVisibility(View.INVISIBLE);
        }

        setContentView(layout);
    }

    public String moveFile(String filePath, String newFolderLocation) {

        Path from = Paths.get(filePath);
        String newFileName = ImageDisplay.generateFileName() + "." + getExtension(from.getFileName().toString());
        Path to = Paths.get(newFolderLocation + "/" + newFileName);
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
            return newFileName;
        } catch (IOException e) {
            Log.e("Move Error", "Move Error");
            e.printStackTrace();
            return "";
        }
    }

    public String copyFile(String filePath, String newFolderLocation) {

        Path from = Paths.get(filePath);
        String newFileName = ImageDisplay.generateFileName() + "." + getExtension(from.getFileName().toString());
        Path to = Paths.get(newFolderLocation + "/" + newFileName);
        try {
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
            return newFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getExtension(String file) {
        String[] splits = file.split("\\.");
        return splits[splits.length - 1];
    }

    public interface MoveOrCopyCallBack {
        void dismissCallback(String method);

        void copiedCallback(String newImagePath);

        void removedCallback(String oldImagePath, String newImagePath);
    }
}