package com.example.photosGroup3;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.Objects;

public class DownloadBR extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        ImageDisplay ic = ImageDisplay.getInstance();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            String[] result = new String[1];
            File kl = new File(ic.fullNameFile);
//            ic.size.add(Integer.parseInt(String.valueOf(kl.length() / 1024)));
            result[0] = ic.fullNameFile;
            if (Objects.equals(ic.fullNameFile, "")) {
                Toast.makeText(ic.getContext(), "Download error, please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(ic.getContext(), "Success", Toast.LENGTH_SHORT).show();
            ((MainActivity) ic.requireContext()).addImageUpdate(result);
            ic.notifyChangeGridLayout();
        }

    }
}