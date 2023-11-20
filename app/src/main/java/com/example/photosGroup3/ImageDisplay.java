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
import com.example.photosGroup3.Utils.ImageName;
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

public class ImageDisplay extends Fragment implements chooseAndDelete {
    Context context;
    @SuppressLint("StaticFieldLeak")
    private static volatile ImageDisplay INSTANCE = null;
    @SuppressLint("StaticFieldLeak")
    private static ImageDisplay MAIN_INSTANCE = null;
    String fullNameFile = "";
    ImageButton changeBtn;
    FloatingActionButton fab_camera, fab_expand, fab_url;
    GridView gridView;
    CardView cardView;
    ArrayList<String> names = new ArrayList<>();
    int numCol = 2;
    ArrayList<String> images;
    String namePictureShoot = "";
    Bundle myStateInfo;
    LayoutInflater myStateInflater;
    ViewGroup myStateContainer;
    CustomAdapter customAdapter = null;
    ListAdapter listAdapter = null;
    ArrayList<ImageDate> imgDates;
    ArrayList<ImageName> imgNames;
    ArrayList<String> dates;
    ArrayList<Integer> size;
    TableLayout header;
    LongClickCallback callback = null;
    public boolean isHolding = false;
    public static boolean isMain = true;
    ArrayList<String> selectedImages = new ArrayList<>();

    String sortType="Date";
    ImageButton sortBtn;
    private ImageDisplay() {
    }

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

    public class CustomAdapter extends BaseAdapter {
        private final ArrayList<String> imagePhotos;
        private final LayoutInflater layoutInflater;
        private class ViewHolder {
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
            if (view == null) {
                view = layoutInflater.inflate(R.layout.row_item, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = view.findViewById(R.id.imageView);
                viewHolder.check = view.findViewById(R.id.checkImage);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            if (isHolding) {
                viewHolder.check.setVisibility(View.VISIBLE);

                viewHolder.check.setChecked(selectedImages.contains(imagePhotos.get(i)));

                viewHolder.check.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (compoundButton.isPressed()) {
                        if (i < imagePhotos.size()) {

                            if (b) {
                                if (!selectedImages.contains(imagePhotos.get(i))) {
                                    selectedImages = ((MainActivity) requireContext()).
                                            adjustChooseToDeleteInList(imagePhotos.get(i),
                                                    "choose");
                                }

                            } else {
                                if (selectedImages.contains(imagePhotos.get(i))) {
                                    selectedImages = ((MainActivity) requireContext()).
                                            adjustChooseToDeleteInList(imagePhotos.get(i),
                                                    "unchoose");
                                }
                            }
                            ((MainActivity) requireContext()).SelectedTextChange();
                            notifyChangeGridLayout();

                        }
                    }

                });
            } else {
                viewHolder.check.setVisibility(View.INVISIBLE);
            }
            File imgFile = new File(imagePhotos.get(i));
            ImageLoader.getInstance().displayImage(String.valueOf(Uri.parse(
                    "file://" + imgFile.getAbsolutePath())),
                    viewHolder.imageView);
            return view;
        }
    }

    public void setLongClickCallBack(LongClickCallback callback) {
        this.callback = callback;
    }

    public class ListAdapter extends BaseAdapter {
        private final ArrayList<String> imageNames;
        private final ArrayList<String> imagePhotos;
        private final LayoutInflater layoutInflater;

        private class ViewHolder {
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
            if (view == null) {
                view = layoutInflater.inflate(R.layout.list_item, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = view.findViewById(R.id.imageView);
                viewHolder.textView = view.findViewById(R.id.tvName);
                viewHolder.check = view.findViewById(R.id.checkImage);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            TextView tvName = viewHolder.textView;
            tvName.setText(imageNames.get(i));
            if (isHolding) {
                viewHolder.check.setVisibility(View.VISIBLE);

                viewHolder.check.setChecked(selectedImages.contains(imagePhotos.get(i)));
                viewHolder.check.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (compoundButton.isPressed()) {
                        if (i < imagePhotos.size()) {

                            if (b) {
                                if (!selectedImages.contains(imagePhotos.get(i))) {
                                    selectedImages = ((MainActivity) requireContext()).
                                            adjustChooseToDeleteInList(imagePhotos.get(i),
                                                    "choose");
                                }

                            } else {
                                if (selectedImages.contains(imagePhotos.get(i))) {
                                    selectedImages = ((MainActivity) requireContext()).
                                            adjustChooseToDeleteInList(imagePhotos.get(i),
                                                    "unhoose");
                                }
                            }
                            ((MainActivity) requireContext()).SelectedTextChange();
                            notifyChangeGridLayout();
                        }
                    }
                });
            } else {
                viewHolder.check.setVisibility(View.INVISIBLE);
            }
            File imgFile = new File(imagePhotos.get(i));
            ImageLoader.getInstance().displayImage(String.valueOf(Uri.parse(
                    "file://" + imgFile.getAbsolutePath())),
                    viewHolder.imageView);
            return view;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();

        if (images == null) {
            assert context != null;
            setImagesData(((MainActivity) context).getFileinDir());
            Toast.makeText(getContext(), "Complete get file", Toast.LENGTH_SHORT).show();
        }

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

        gridView = view.findViewById(R.id.gridView);
        changeBtn = view.findViewById(R.id.resizeView);
        cardView = view.findViewById(R.id.cardView);
        fab_camera = view.findViewById(R.id.fab_Camera);
        sortBtn = view.findViewById(R.id.sortView);
        fab_expand = view.findViewById(R.id.fab_expand);
        fab_url = view.findViewById(R.id.fab_url);

        if (customAdapter == null) {
            customAdapter = new CustomAdapter(images, requireActivity());

        } else {
            customAdapter.notifyDataSetChanged();
        }
        if (listAdapter == null) {
            listAdapter = new ListAdapter(names, images, requireActivity());
        } else {
            listAdapter.notifyDataSetChanged();
        }

        gridView.setAdapter(customAdapter);
        gridView.setOnItemClickListener((adapterView, view15, i, l) -> {

            if (!isHolding) {
                someActivityResultLauncher.launch(new Intent(getActivity(), SelectedPicture.class)
                        .putExtra("size", size)
                        .putExtra("images", images)
                        .putExtra("dates", dates)
                        .putExtra("pos", i));
                Toast.makeText(context, "running", Toast.LENGTH_SHORT).show();
            }
        });


        gridView.setOnItemLongClickListener((adapterView, view16, i, l) -> {
            isHolding = true;
            ((MainActivity) requireContext()).Holding(isHolding);

            String selectedName = images.get(i);

            selectedImages = ((MainActivity) requireContext()).
                    adjustChooseToDeleteInList(selectedName, "choose");

            ((MainActivity) requireContext()).SelectedTextChange();

            notifyChangeGridLayout();


            return true;
        });


        changeBtn.setOnClickListener(view14 -> {
            numCol = numCol % 5 + 1;
            if (numCol == 1) {
//                    numCol=2;
                gridView.setAdapter(listAdapter);

            } else if (numCol == 2) {
                gridView.setAdapter(customAdapter);
            }
            gridView.setNumColumns(numCol);
        });

        sortBtn.setOnClickListener(view1 -> {
            if (sortType.equals("Date")){
                sortType="Name";
                Toast.makeText(getContext(), "Sort by name", Toast.LENGTH_SHORT).show();
            }
            else if (sortType.equals("Name")){
                sortType="Date";
                Toast.makeText(getContext(), "Sort by date", Toast.LENGTH_SHORT).show();
            }
            sortImage();
            notifyChangeGridLayout();
        });


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

        header = view.findViewById(R.id.header);

        return view;
    }

    private void showInputDialogBox() {
        final String[] url_input = {"", ""};
        final Dialog customDialog = new Dialog(getContext());
        customDialog.setTitle("Delete confirm");

        customDialog.setContentView(R.layout.url_download_diagbox);
        Objects.requireNonNull(customDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        customDialog.findViewById(R.id.download_url_cancel)
                .setOnClickListener(view -> {
                    customDialog.dismiss();
                });

        customDialog.findViewById(R.id.download_url_confirm)
                .setOnClickListener(view -> {
                    url_input[0] = ((EditText) customDialog.findViewById(R.id.download_url_input)).getText().toString();
                    url_input[1] = ((EditText) customDialog.findViewById(R.id.download_url_rename)).getText().toString();
                    Toast.makeText(INSTANCE.getContext(), url_input[0], Toast.LENGTH_SHORT).show();
                    DownloadImageFromURL(url_input[0].trim(), url_input[1].trim());

                    customDialog.dismiss();
                });

        customDialog.show();
    }

    private void DownloadImageFromURL(String input, String fileName) {
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
        Notification noti = new NotificationCompat.Builder(requireContext(), "Download " + fullNameFile)
                .setContentText("Downloaded item")
                .setSmallIcon(R.drawable.ic_launcher_background).build();
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
        customAdapter.notifyDataSetChanged();
        listAdapter.notifyDataSetChanged();
    }

    public void notifyChanged() {
        customAdapter.notifyDataSetChanged();
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
                        imageShoot = ImageUltility.rotateImage(imageShoot, 90);
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
                            names.add(getDisplayName(imgFile.getAbsolutePath()));
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
        customAdapter.notifyDataSetChanged();
        listAdapter.notifyDataSetChanged();
    }


    public void setImagesData(ArrayList<String> images) {
        this.images = images;
        ArrayList<Date> listDate = new ArrayList<>();
        size = new ArrayList<>(this.images.size());
        for (int i = 0; i < this.images.size(); i++) {
            File file = new File(this.images.get(i));
            if (file.exists())
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
        for (int i = 0; i < this.images.size(); i++) {
            ImageDate temp = new ImageDate(this.images.get(i), listDate.get(i));
            imgDates.add(temp);
            dates.add(temp.dayToString());
        }

//        Collections.sort(imgDates);
//        Collections.reverse(imgDates);
//        for (int i = 0; i < imgDates.size(); i++) {
//            this.images.set(i, imgDates.get(i).getImage());
//
//        }
        names = new ArrayList<>();
        imgNames = new ArrayList<>();

        for (int i = 0; i < this.images.size(); i++) {
            ImageName temp = new ImageName(this.images.get(i), getDisplayName(this.images.get(i)));
            imgNames.add(temp);
            String name = getDisplayName(this.images.get(i));
            names.add(name);
        }
        sortImage();
    }

    private void sortImage(){
        if (sortType.equals("Date")){
            //sort obj
            Collections.sort(imgDates);
            Collections.reverse(imgDates);

            //change images after sort
            for (int i = 0; i < imgDates.size(); i++) {
                this.images.set(i, imgDates.get(i).getImage());
            }
        }
        else if (sortType.equals("Name")){
            Collections.sort(imgNames);
            Collections.reverse(imgNames);

            //change images after sort
            for (int i = 0; i < imgNames.size(); i++) {
                this.images.set(i, imgNames.get(i).getImage());
            }
        }
    }

    public void addNewImage(String imagePath, int status) {
        File file = new File(imagePath);
        if (!file.exists())
        {
            return;
        }
        if (!images.contains(imagePath)) {
            if (status == 0) {
                if (!MainActivity.checkInHash((imagePath))) {
                    return;
                }
            }
            ExifInterface intf = null;
            try {
                intf = new ExifInterface(imagePath);
                Date lastModDate = new Date(file.lastModified());


                size.add(0, ((Number) file.length()).intValue());
                dates.add(0, lastModDate.toString());
            } catch (IOException ignored) {

            }
            if (intf == null) {
                Date lastModDate = new Date(file.lastModified());
                dates.add(0, lastModDate.toString());
            }
            images.add(0, imagePath);
            names.add(0, getDisplayName(imagePath));
            notifyChanged();
        }
    }

    public void removeImage(String name) {
        int index = this.images.indexOf(name);
        if (index != -1) {
            ((MainActivity) requireContext()).removeInHash(name);
            this.images.remove(index);
            this.names.remove(index);
            this.dates.remove(index);
            this.size.remove(index);
            notifyChanged();
        }
    }

    public interface LongClickCallback {
        void onLongClick();
        void afterLongClick();
    }
}