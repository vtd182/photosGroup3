package com.example.photosGroup3.Utils;

import android.app.Activity;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

public final class ImageDelete extends Activity {
    static Bitmap blurfast(Bitmap bmp, int radius) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int[] pix = new int[w * h];
        bmp.getPixels(pix, 0, w, 0, 0, w, h);

        for(int r = radius; r >= 1; r /= 2) {
            for(int i = r; i < h - r; i++) {
                for(int j = r; j < w - r; j++) {
                    int tl = pix[(i - r) * w + j - r];
                    int tr = pix[(i - r) * w + j + r];
                    int tc = pix[(i - r) * w + j];
                    int bl = pix[(i + r) * w + j - r];
                    int br = pix[(i + r) * w + j + r];
                    int bc = pix[(i + r) * w + j];
                    int cl = pix[i * w + j - r];
                    int cr = pix[i * w + j + r];

                    pix[(i * w) + j] = 0xFF000000 |
                            (((tl & 0xFF) + (tr & 0xFF) + (tc & 0xFF) + (bl & 0xFF) + (br & 0xFF) + (bc & 0xFF) + (cl & 0xFF) + (cr & 0xFF)) >> 3) & 0xFF |
                            (((tl & 0xFF00) + (tr & 0xFF00) + (tc & 0xFF00) + (bl & 0xFF00) + (br & 0xFF00) + (bc & 0xFF00) + (cl & 0xFF00) + (cr & 0xFF00)) >> 3) & 0xFF00 |
                            (((tl & 0xFF0000) + (tr & 0xFF0000) + (tc & 0xFF0000) + (bl & 0xFF0000) + (br & 0xFF0000) + (bc & 0xFF0000) + (cl & 0xFF0000) + (cr & 0xFF0000)) >> 3) & 0xFF0000;
                }
            }
        }
        bmp.setPixels(pix, 0, w, 0, 0, w, h);
        return bmp;
    }


    public static boolean DeleteImage(String[] ListImage){
        boolean running=true;
        for (String file : ListImage)
        {
            File fdel=new File(file);
            if(fdel.exists()){

                fdel.delete();
            }
        }
        return running;
    }

    public static long hashBitmap(Bitmap bitmap) {
        long hash_result = 0;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        hash_result = (hash_result << 7) ^ h;
        hash_result = (hash_result << 7) ^ w;
        for (int pixel = 0; pixel < 20; ++pixel) {
            int x = (pixel * 50) % w;
            int y = (pixel * 100) % h;
            hash_result = (hash_result << 7) ^ bitmap.getPixel(x, y);
        }
        return hash_result;
    }
    //xử lí single image

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
