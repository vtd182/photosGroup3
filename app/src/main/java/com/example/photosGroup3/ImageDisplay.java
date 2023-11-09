package com.example.photosGroup3;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
//noinspection ExifInterface
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.photosGroup3.Callback.chooseAndDelete;
import com.example.photosGroup3.Utils.ImageDate;
import com.example.photosGroup3.Utils.ImageUltility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

/*
* File imgFile= new File(Images.get(position));
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        holder.imageItem.setImageBitmap(myBitmap);
*
* */



public class ImageDisplay extends Fragment {
    Context context;

    @SuppressLint("StaticFieldLeak")
    private static volatile ImageDisplay INSTANCE = null;
    @SuppressLint("StaticFieldLeak")
    private static ImageDisplay MAIN_INSTANCE=null;

    String fullNameFile="";

    ImageButton changeBtn;
    FloatingActionButton fab_camera,fab_expand,fab_url;
    GridView gridView;
    CardView cardView;
    ArrayList<String> names = new ArrayList<>();
    int numCol=2;
    ArrayList<String> images;
    String namePictureShoot="";
    Bundle myStateInfo;
    LayoutInflater myStateInflater;
    ViewGroup myStatecontainer;
    ImageDisplay.CustomAdapter customAdapter=null;
    ImageDisplay.ListAdapter listAdapter=null;

    ArrayList<ImageDate> imgDates;
    ArrayList<String> dates;
    ArrayList<Integer> size;

    TableLayout header;
    LongClickCallback callback=null;


    public boolean isHolding=false;
    public static boolean isMain=true;

    ArrayList<String> selectedImages=new ArrayList<>();


    //universal-image-loader
    // Create default options which will be used for every
    //  displayImage(...) call if no options will be passed to this method

    private ImageDisplay() {
        // Required empty public constructors
    }

    // TODO: Rename and change types and number of parameters

    public static ImageDisplay getInstance() {
        if(INSTANCE==null)
        {
            synchronized (ImageDisplay.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ImageDisplay();
                }
            }
        }
        return INSTANCE;
    }

    public static ImageDisplay newInstance() {
        INSTANCE = new ImageDisplay();
        return INSTANCE;
    }

    public static void changeINSTANCE(){
        if(isMain)
        {
            MAIN_INSTANCE=INSTANCE;
            isMain=false;
            INSTANCE=null;
        }
    }
    public static void restoreINSTANCE(){
        if(!isMain){
            INSTANCE=MAIN_INSTANCE;
            isMain=true;
        }
    }

    public class CustomAdapter extends BaseAdapter {
        private final ArrayList<String> imagePhotos;

        private final LayoutInflater layoutInflater;

        private class ViewHolder{
            ImageView imageView;
            CheckBox check;
        }

        public CustomAdapter(ArrayList<String> imagePhotos, @NonNull Context context) {
            this.imagePhotos = imagePhotos;

            this.layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return imagePhotos.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
           ViewHolder viewHolder;
//            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            if(view == null){
                view =layoutInflater.inflate(R.layout.row_item,viewGroup,false);
                viewHolder=new ViewHolder();
                viewHolder.imageView=view.findViewById(R.id.imageView);
                viewHolder.check=view.findViewById(R.id.checkImage);
                view.setTag(viewHolder);
            } else {
                viewHolder=(ViewHolder) view.getTag();

            }
            if(isHolding)
            {
                viewHolder.check.setVisibility(View.VISIBLE);

                viewHolder.check.setChecked(selectedImages.contains(imagePhotos.get(i)));

                viewHolder.check.setOnCheckedChangeListener((compoundButton, b) -> {
                    if(compoundButton.isPressed())
                    {
                        if(i <imagePhotos.size()) {

                            if (b) {
                                if (!selectedImages.contains(imagePhotos.get(i))) {
                                    selectedImages= ((MainActivity) requireContext()).adjustChooseToDeleteInList(imagePhotos.get(i),"choose");
                                    //selectedImages.add(imagePhotos.get(currentView));
                                }

                            } else {
                                if (selectedImages.contains(imagePhotos.get(i))) {
                                    selectedImages=  ((MainActivity) requireContext()).adjustChooseToDeleteInList(imagePhotos.get(i),"unchoose");

                                    //selectedImages.remove(imagePhotos.get(currentView));
                                }


                            }
                            ((MainActivity) requireContext()).SelectedTextChange();
                            notifyChangeGridLayout();

                        }
                    }

                });
            }
            else
            {
                viewHolder.check.setVisibility(View.INVISIBLE);
            }
            File imgFile= new File(imagePhotos.get(i));
          ImageLoader.getInstance().displayImage(String.valueOf(Uri.parse("file://"+ imgFile.getAbsolutePath())),viewHolder.imageView);


            return view;
        }



    }

    public interface LongClickCallback {
        void onLongClick();
        void afterLongClick();
    }
    public void setLongClickCallBack(LongClickCallback callback){
        this.callback=callback;
    }

    public class ListAdapter extends BaseAdapter{
        private final ArrayList<String> imageNames;
        private final ArrayList<String> imagePhotos;
        private final LayoutInflater layoutInflater;
        private class ViewHolder{
            TextView textView;
            ImageView imageView;
            CheckBox check;
        }

        public ListAdapter(ArrayList<String> imageNames, ArrayList<String> imagePhotos, Context context) {
            this.imageNames = imageNames;
            this.imagePhotos = imagePhotos;
            this.layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return imagePhotos.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if(view == null){
                view =layoutInflater.inflate(R.layout.list_item,viewGroup,false);
                viewHolder=new ViewHolder();
                viewHolder.imageView=view.findViewById(R.id.imageView);
                viewHolder.textView=view.findViewById(R.id.tvName);
                viewHolder.check=view.findViewById(R.id.checkImage);
                view.setTag(viewHolder);
            } else {
                viewHolder=(ViewHolder) view.getTag();
            }
            TextView tvName = viewHolder.textView;
            tvName.setText(imageNames.get(i));
            if(isHolding)
            {
                viewHolder.check.setVisibility(View.VISIBLE);

                viewHolder.check.setChecked(selectedImages.contains(imagePhotos.get(i)));
                viewHolder.check.setOnCheckedChangeListener((compoundButton, b) -> {
                    if(compoundButton.isPressed())
                    {
                        if(i <imagePhotos.size()) {

                            if (b) {
                                if (!selectedImages.contains(imagePhotos.get(i))) {
                                    selectedImages= ((MainActivity) requireContext()).adjustChooseToDeleteInList(imagePhotos.get(i),"choose");
                                    //selectedImages.add(imagePhotos.get(currentView));
                                }

                            } else {
                                if (selectedImages.contains(imagePhotos.get(i))) {
                                    selectedImages=  ((MainActivity) requireContext()).adjustChooseToDeleteInList(imagePhotos.get(i),"unhoose");

                                    //selectedImages.remove(imagePhotos.get(currentView));
                                }


                            }
                            ((MainActivity) requireContext()).SelectedTextChange();
                            notifyChangeGridLayout();

                        }
                    }

                });
            }
            else
            {
                viewHolder.check.setVisibility(View.INVISIBLE);
            }
            File imgFile= new File(imagePhotos.get(i));
            ImageLoader.getInstance().displayImage(String.valueOf(Uri.parse("file://"+ imgFile.getAbsolutePath())),viewHolder.imageView);
            return view;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context= getActivity();

        if(images == null) {
            assert context != null;
            setImagesData (((MainActivity)context).getFileinDir());
            Toast.makeText(getContext(),"Complete get file",Toast.LENGTH_SHORT).show();
        }

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //Toast.makeText(getContext(),"ImageDisplay oncreatview",Toast.LENGTH_SHORT).show();
        myStateInflater =inflater;
        myStatecontainer=container;
        myStateInfo = savedInstanceState;
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_image_display, container, false);

        gridView = view.findViewById(R.id.gridView);
        changeBtn = view.findViewById(R.id.resizeView);
        cardView = view.findViewById(R.id.cardView);
        fab_camera= view.findViewById(R.id.fab_Camera);
        fab_expand= view.findViewById(R.id.fab_expand);
        fab_url= view.findViewById(R.id.fab_url);

        if(customAdapter==null)
        {
            customAdapter = new ImageDisplay.CustomAdapter(images, requireActivity());

        } else {
            customAdapter.notifyDataSetChanged();
        }
        if(listAdapter==null)
        {
            listAdapter = new ImageDisplay.ListAdapter(names,images, requireActivity());
        } else {
            listAdapter.notifyDataSetChanged();
        }

        gridView.setAdapter(customAdapter);
        gridView.setOnItemClickListener((adapterView, view15, i, l) -> {
        });



        gridView.setOnItemLongClickListener((adapterView, view16, i, l) -> {
            isHolding =true;
            ((MainActivity) requireContext()).Holding(isHolding);

            String selectedName= images.get(i);

            selectedImages= ((MainActivity) requireContext()).adjustChooseToDeleteInList(selectedName,"choose");

            ((MainActivity) requireContext()).SelectedTextChange();

            notifyChangeGridLayout();



            return true;
        });



        changeBtn.setOnClickListener(view14 -> {
            numCol=numCol%5+1;
            if(numCol==1){
//                    numCol=2;
                gridView.setAdapter(listAdapter);

            } else if(numCol == 2) {
                gridView.setAdapter(customAdapter);
            }
            gridView.setNumColumns(numCol);
        });

        fab_url.setVisibility(View.INVISIBLE);
        fab_camera.setVisibility(View.INVISIBLE);
        fab_expand.setOnClickListener(view13 -> {
            if (fab_camera.getVisibility() == View.INVISIBLE){
                fab_url.setVisibility(View.VISIBLE);
                fab_camera.setVisibility(View.VISIBLE);
            } else {
                fab_url.setVisibility(View.INVISIBLE);
                fab_camera.setVisibility(View.INVISIBLE);
            }
        });

        header= view.findViewById(R.id.header);

        return view;
//        return inflater.inflate(R.layout.fragment_image_display, container, false);
    }














    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //                        ((MainActivity) getContext()).readAgain();
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes

                        File imgFile= new File(namePictureShoot);
                        Bitmap imageShoot= BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        imageShoot= ImageUltility.rotateImage(imageShoot,90);
                        FileOutputStream out;
                        try {
                            out = new FileOutputStream(imgFile);
                            imageShoot.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(!images.contains(imgFile.getAbsolutePath()))
                        {
                            images.add(imgFile.getAbsolutePath());
                            names.add(getDisplayName(imgFile.getAbsolutePath()));

                        }

                        notifyChangeGridLayout();
                        setImagesData (((MainActivity)context).getFileinDir());

                        Toast.makeText(getContext(), "Taking picture", Toast.LENGTH_SHORT).show();
                    }
                }
            });



    public static String generateFileName(){
        LocalDateTime now=LocalDateTime.now();
        DateTimeFormatter myFormat=DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
        return now.format(myFormat);
    }
    // Android 10+
    private Uri getUri(String path){

        ContentValues values = new ContentValues();
        String tempName=generateFileName()+".jpg";
        namePictureShoot= ((MainActivity) requireContext()).getCurrentDirectory()+'/'+tempName;
        values.put(MediaStore.Images.Media.DISPLAY_NAME,tempName );
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, path);

        return requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
    }
    public static String getDisplayName(String path){
        int getPositionFolderName= path.lastIndexOf("/");

        return path.substring(getPositionFolderName + 1);
    }

    public void notifyChangeGridLayout(){
        customAdapter.notifyDataSetChanged();
        listAdapter.notifyDataSetChanged();
    }


    public void setImagesData(ArrayList<String> images) {
        this.images = images;
        //get date
        ArrayList<Date> listDate = new ArrayList<>();
        size = new ArrayList<>(this.images.size());
        for (int i = 0; i < this.images.size(); i++) {
            File file = new File(this.images.get(i));
            if (file.exists()) //Extra check, Just to validate the given path
            {
                ExifInterface intf = null;
                try {
                    intf = new ExifInterface(this.images.get(i));
                    Date lastModDate = new Date(file.lastModified());


                    size.add(((Number) file.length()).intValue());
                    listDate.add(lastModDate);
                } catch (IOException ignored) {

                }
                if (intf == null) {
                    Date lastModDate = new Date(file.lastModified());
                    listDate.add(lastModDate);
                }
            }
        }
        imgDates = new ArrayList<>();

        dates = new ArrayList<>();
        //get object
        for (int i = 0; i < this.images.size(); i++) {
            ImageDate temp = new ImageDate(this.images.get(i), listDate.get(i));
            imgDates.add(temp);
            dates.add(temp.dayToString());
        }

        //sort obj
        Collections.sort(imgDates);
        Collections.reverse(imgDates);

        //change images after sort
        for (int i = 0; i < imgDates.size(); i++) {
            this.images.set(i, imgDates.get(i).getImage());

        }

//        Collections.sort(images);
        //checkPhoto=new ArrayList<Boolean>(Arrays.asList(new Boolean[images.size()]));
        //Collections.fill(checkPhoto, Boolean.FALSE);

        //create name array
        names = new ArrayList<>();

        for (int i = 0; i < this.images.size(); i++) {

            // get name from file ===================================
            String name = getDisplayName(this.images.get(i));
            names.add(name);
            // ====================================================
        }
    }
}