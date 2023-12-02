package com.example.photosGroup3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.Toast;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photosGroup3.Callback.chooseAndDelete;
import com.example.photosGroup3.Utils.ImageUtility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

public class ImageDisplay extends Fragment implements chooseAndDelete {
    Context context;
    @SuppressLint("StaticFieldLeak")
    private static volatile ImageDisplay INSTANCE = null;
    @SuppressLint("StaticFieldLeak")
    private static ImageDisplay MAIN_INSTANCE = null;
    String fullNameFile = "";
    ImageButton changeBtn;
    FloatingActionButton fab_camera, fab_expand, fab_url;
    RecyclerView recyclerView;
    CardView cardView;
    int numCol = 2;
    ArrayList<String> images;
    String namePictureShoot = "";
    Bundle myStateInfo;
    LayoutInflater myStateInflater;
    ViewGroup myStateContainer;
    ListAdapter listAdapter = null;

    GridLayoutManager gridlayoutManager = null;
    TableLayout header;
    LongClickCallback callback = null;
    public boolean isHolding = false;
    public static boolean isMain = true;
    ArrayList<String> selectedImages = new ArrayList<>();

    String sortType = "Date";
    ImageButton sortBtn;

    boolean isGridView = true;

    ArrayList<String> results ;

    public ImageDisplay() {
    }

    public  FloatingActionButton getActionButton(){
        return fab_expand;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    Toolbar toolbar;

    public static ImageDisplay getInstance() {
        if (INSTANCE == null) {
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

    public static void changeINSTANCE() {
        if (isMain) {
            MAIN_INSTANCE = INSTANCE;
            isMain = false;
            INSTANCE = null;
        }
    }

    public static void restoreINSTANCE() {
        if (!isMain) {
            INSTANCE = MAIN_INSTANCE;
            isMain = true;
        }
    }

    public void setLongClickCallBack(LongClickCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        INSTANCE = this;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection deprecation
        setHasOptionsMenu(true);
        if (images == null) {
            assert context != null;
            setImagesData(((MainActivity) context).getFileinDir());
        }

    }

    /** @noinspection deprecation*/
    @Override
    public void onCreateOptionsMenu(@NonNull android.view.Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_for_main_activity, menu);
        //noinspection deprecation
        super.onCreateOptionsMenu(menu, inflater);
    }
    /** @noinspection deprecation*/
    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.item_sort) {
            if (sortType.equals("Name")){
                sortType = "Date";
                item.setTitle("Sắp xếp theo tên");}
            else{
                sortType = "Name";
            item.setTitle("Sắp xếp theo thời gian");}
            sortImage();
            notifyChangeGridLayout();
        } else if (item.getItemId() == R.id.item_setting) {
            // Chuyển đến fragment khác (ví dụ: YourSettingsFragment)
            SettingsFragment settingsFragment = new SettingsFragment();
            FragmentTransaction transaction = requireFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, settingsFragment);
            transaction.addToBackStack(null); // Để thêm fragment vào stack để có thể quay lại
            transaction.commit();
        } else if (item.getItemId() == R.id.item_view) {
            if (!isGridView) {
                listAdapter.setGrid(true);
                gridlayoutManager.setSpanCount(4);
                isGridView = true;
                item.setTitle("Xem dạng danh sách");
            } else {
                listAdapter.setGrid(false);
                gridlayoutManager.setSpanCount(1);
                isGridView = false;
                item.setTitle("Xem dạng lưới");
            }
        }
        return super.onOptionsItemSelected(item);
    }



    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        myStateInflater = inflater;
        myStateContainer = container;
        myStateInfo = savedInstanceState;
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_image_display, container, false);

        toolbar = view.findViewById(R.id.toolbar1);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        assert activity != null;
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }


        recyclerView = view.findViewById(R.id.gridView);
        //changeBtn = view.findViewById(R.id.resizeView);
        cardView = view.findViewById(R.id.cardView);
        fab_camera = view.findViewById(R.id.fab_Camera);
        //sortBtn = view.findViewById(R.id.sortView);
        fab_expand = view.findViewById(R.id.fab_expand);
        fab_url = view.findViewById(R.id.fab_url);

        if (listAdapter == null) {
            listAdapter = new ListAdapter(this, images,true,getContext());
        } else {
            listAdapter.notifyDataSetChanged();
        }

        if (gridlayoutManager == null)
            gridlayoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(gridlayoutManager);
        recyclerView.setAdapter(listAdapter);



        fab_camera.setOnClickListener(view12 -> openCamera());

        fab_url.setOnClickListener(view1 -> showInputDialogBox());
        fab_url.setVisibility(View.INVISIBLE);
        fab_camera.setVisibility(View.INVISIBLE);
        fab_expand.setOnClickListener(view13 -> {
            if (fab_camera.getVisibility() == View.INVISIBLE) {
                fab_url.setVisibility(View.VISIBLE);
                fab_camera.setVisibility(View.VISIBLE);
            } else {
                fab_url.setVisibility(View.INVISIBLE);
                fab_camera.setVisibility(View.INVISIBLE);
            }
        });

        //header = view.findViewById(R.id.header);


        return view;
    }

    private void showInputDialogBox() {
        final String[] url_input = {"", ""};
        final Dialog customDialog = new Dialog(requireContext());
        customDialog.setTitle("Delete confirm");

        customDialog.setContentView(R.layout.url_download_diagbox);
        Objects.requireNonNull(customDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        customDialog.findViewById(R.id.download_url_cancel)
                .setOnClickListener(view -> customDialog.dismiss());

        customDialog.findViewById(R.id.download_url_confirm)
                .setOnClickListener(view -> {
                    url_input[0] = ((EditText) customDialog.findViewById(R.id.download_url_input)).getText().toString();
                    url_input[1] = ((EditText) customDialog.findViewById(R.id.download_url_rename)).getText().toString();
                    DownloadImageFromURL(url_input[0].trim(), url_input[1].trim());
                    customDialog.dismiss();
                });

        customDialog.show();
    }

    private void DownloadImageFromURL(String input, String fileName) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(input));
            String fileExtension = input.substring(input.lastIndexOf("."));
            while (fileExtension.charAt(fileExtension.length() - 1) == '\n') {
                fileExtension = fileExtension.substring(0, fileExtension.length() - 1);
            }

            if (fileName.length() == 0) {
                fileName = (new Date()).getTime() + "";

            }
            fullNameFile = ((MainActivity) requireContext()).getPictureDirectory() + "/" + fileName + fileExtension;
            request.setDescription("Downloading " + input + "...");
            request.setTitle(input);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(Uri.fromFile(new File(fullNameFile)));
            DownloadManager manager = (DownloadManager) INSTANCE.requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            new NotificationCompat.Builder(requireContext(), "Download " + fullNameFile)
                    .setContentText("Downloaded item")
                    .setSmallIcon(R.drawable.ic_launcher_background).build();

        } catch (Exception e) {
            Toast.makeText(INSTANCE.getContext(), "Download error, please try again", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void deleteClicked() {

        isHolding = false;
        ((MainActivity) requireContext()).Holding(isHolding);
        selectedImages = ((MainActivity) requireContext()).chooseToDeleteInList();
        notifyChangeGridLayout();
    }

    @Override
    public void deleteClicked(String file) {
        ((MainActivity) requireContext()).removeImageUpdate(file);
        notifyChangeGridLayout();
    }

    @Override
    public void renameClicked(String file, String newFile) {
        ((MainActivity) requireContext()).renameImageUpdate(file, newFile);
    }

    @Override
    public void clearClicked() {
        isHolding = false;
        ((MainActivity) requireContext()).Holding(isHolding);
        notifyChangeGridLayout();
        listAdapter.notifyDataSetChanged();
    }

    public void notifyChanged() {
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void selectAllClicked() {
        isHolding = true;
        ((MainActivity) requireContext()).Holding(isHolding);
        selectedImages = ((MainActivity) requireContext()).chooseToDeleteInList();
        ((MainActivity) requireContext()).SelectedTextChange();
        notifyChangeGridLayout();
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        File imgFile = new File(namePictureShoot);
                        Bitmap imageShoot = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        imageShoot = ImageUtility.rotateImage(imageShoot, 90);
                        FileOutputStream out;
                        try {
                            out = new FileOutputStream(imgFile);
                            imageShoot.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (!images.contains(imgFile.getAbsolutePath())) {
                            images.add(imgFile.getAbsolutePath());
                        }
                        notifyChangeGridLayout();
                        setImagesData(((MainActivity) context).getFileinDir());

                        Toast.makeText(getContext(), "Taking picture", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, getUri(Environment.DIRECTORY_PICTURES));
        someActivityResultLauncher.launch(intent);
    }

    public static String generateFileName() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
        return now.format(myFormat);
    }

    private Uri getUri(String path) {

        ContentValues values = new ContentValues();
        String tempName = generateFileName() + ".jpg";
        namePictureShoot = ((MainActivity) requireContext()).getCurrentDirectory() + '/' + tempName;
        values.put(MediaStore.Images.Media.DISPLAY_NAME, tempName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, path);

        return requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public static String getDisplayName(String path) {
        int getPositionFolderName = path.lastIndexOf("/");

        return path.substring(getPositionFolderName + 1);
    }

    public void notifyChangeGridLayout() {
        listAdapter.notifyDataSetChanged();
    }


    public void setImagesData(ArrayList<String> images) {
        this.images = images;
        for (int i = 0; i < this.images.size(); i++) {
            File file = new File(this.images.get(i));
            if (file.exists()) {
                ExifInterface intf = null;
                try {
                    intf = new ExifInterface(this.images.get(i));
                    //size.add(((Number) file.length()).intValue());
                } catch (IOException ignored) {

                }
                if (intf == null) {
                    Date lastModDate = new Date(file.lastModified());
                }
            }
        }
        sortImage();
    }

    private void sortImage() {
        if (sortType.equals("Date")) {
            for (int i = 0; i < images.size(); i++) {
                for (int j = i + 1; j < images.size(); j++) {
                    File file1 = new File(images.get(i));
                    File file2 = new File(images.get(j));
                    if (file1.exists() && file2.exists()) {
                        if (file1.lastModified() < file2.lastModified()) {
                            Collections.swap(images, i, j);
                        }
                    }
                }
            }
        } else if (sortType.equals("Name")) {
            for (int i = 0; i < images.size(); i++) {
                for (int j = i + 1; j < images.size(); j++) {
                    File file1 = new File(images.get(i));
                    File file2 = new File(images.get(j));
                    if (file1.exists() && file2.exists()) {
                        if (getDisplayName(images.get(i)).compareTo(getDisplayName(images.get(j))) > 0) {
                            Collections.swap(images, i, j);
                        }
                    }
                }
            }
        }
    }

    public void addNewImage(String imagePath, int status) {
        File file = new File(imagePath);
        if (!file.exists()) {
            return;
        }
        if (!images.contains(imagePath)) {
            if (status == 0) {
                return;
            }
            ExifInterface intf = null;
            try {
                intf = new ExifInterface(imagePath);
            } catch (IOException ignored) {

            }
            if (intf == null) {
            }
            images.add(0, imagePath);
            notifyChanged();
        }
    }

    public void removeImage(String name) {
        int index = this.images.indexOf(name);
        if (index != -1) {
            this.images.remove(index);
            notifyChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void searchImage(String query){

        if (query.isEmpty()){
            results = this.images;
        } else {
            results = new ArrayList<>();
            for (String image: this.images){
                if (image.contains(query)){
                    results.add(image);
                }
            }
        }
        Log.e("Tpoo","Search: " + results.toString());
        listAdapter.setImagePhotos(results);
        listAdapter.notifyDataSetChanged();

    }

    public interface LongClickCallback {
        void onLongClick();

        void afterLongClick();
    }
}