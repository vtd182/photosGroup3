package com.example.photosGroup3.Utils;

import android.app.Activity;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

public final class ImageDelete extends Activity {

    public static boolean DeleteImage(String[] ListImage){
        for (String image:ListImage)
            DeleteImage(image);
        return true;
    }

    public static boolean DeleteImage(String image){
        boolean running=true;
        File fdel=new File(image);
        if(fdel.exists()){
            fdel.delete();
        }
        return running;
    }
    public static String saveImage(Bitmap finalBitmap, String imagePath) {

        File myFile = new File(imagePath);

        int i= 0;
        String oldName=myFile.getName().split("\\.")[0];
        String extension=myFile.getName().split("\\.")[1];
//        String[] delim=imagePath.split("\\.");
        String newName=oldName;
        while( myFile.exists())
        {
            newName+="_"+i;
            i++;
            myFile = new File(myFile.getParent()+"/"+newName+"."+extension);
            newName=oldName;
        }
        try {
            FileOutputStream out = new FileOutputStream(myFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }



        return myFile.getAbsolutePath();
    }

}
