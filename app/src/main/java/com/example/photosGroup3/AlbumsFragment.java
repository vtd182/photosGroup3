package com.example.photosGroup3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photosGroup3.Utils.ItemDecoration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;


public class AlbumsFragment extends Fragment {
    Context context;
    int spanColumns;
    static ArrayList<Album> albumList;
    public static String favourite = "Favourite";
    FloatingActionButton fab_addNewAlbum;
    RecyclerView rcv_albumList;
    public static String folderPath = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .getAbsolutePath() + "/Glr3/Albums";
    @SuppressLint("StaticFieldLeak")
    private static AlbumsFragment INSTANCE = null;

    public AlbumsFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AlbumsFragment getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AlbumsFragment();
            readData();
        }
        // put args
        return INSTANCE;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get args
        context = getActivity();
        //readData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        CoordinatorLayout layout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_albums, container, false);
        // SAMPLE TEST
        spanColumns = 2;
        rcv_albumList = layout.findViewById(R.id.album_list);
        rcv_albumList.setLayoutManager(new LinearLayoutManager(context));
        rcv_albumList.setAdapter(new AlbumAdapter(albumList, context));
        rcv_albumList.addItemDecoration(new ItemDecoration(20, 1));
        fab_addNewAlbum = layout.findViewById(R.id.album_fab_add);
        fab_addNewAlbum.setOnClickListener(view -> showNewFolderDialog());
        return layout;
    }

    private static void readData() {
        albumList = new ArrayList<>();
        File path = new File(folderPath);
        if (!path.isDirectory()) {
            path.mkdirs();
        } else {
            File[] albumFolders = path.listFiles();
            assert albumFolders != null;
            for (File folder : albumFolders) {
                if (!folder.isFile()) {
                    albumList.add(new Album(folder.getAbsolutePath(), folder.getName(), imagePathInFolder(folder)));
                }
            }
        }

        int favoriteIndex = indexOfFavorite(albumList);
        if (favoriteIndex == -1) {
            File favorite = new File(path.getAbsolutePath() + "/" + favourite);
            favorite.mkdirs();
            albumList.add(0, new Album(favorite.getAbsolutePath(), favorite.getName(), imagePathInFolder(favorite)));
        } else {
            Album fav = albumList.get(favoriteIndex);
            albumList.remove(favoriteIndex);
            albumList.add(0, fav);
        }
    }

    private static int indexOfFavorite(ArrayList<Album> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).name.equals(favourite)) {
                return i;
            }
        }
        return -1;
    }

    private static ArrayList<String> imagePathInFolder(File folder) {
        ArrayList<String> result = new ArrayList<>();

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) {

            } else {
                for (String extension : MainActivity.ImageExtensions) {

                    if (file.getAbsolutePath().toLowerCase().endsWith(extension)) {
                        result.add(file.getAbsolutePath());

                        break;
                    }

                }
            }
        }
        return result;
    }


    private void showNewFolderDialog() {
        NewFolderDialog dialog = new NewFolderDialog(context);
        dialog.show();
    }

    private boolean addNewFolder(String newAlbumName) {
        File path = new File(folderPath + "/" + newAlbumName);
        if (path.isDirectory()) {
            return false;
        } else {
            path.mkdirs();
        }
        ArrayList<String> imagePaths = new ArrayList<>();
        albumList.add(new Album(path.getAbsolutePath(), newAlbumName, imagePaths));
        Objects.requireNonNull(rcv_albumList.getAdapter()).notifyItemChanged(albumList.size() - 1);
        return true;
    }


    public class NewFolderDialog extends Dialog {

        public NewFolderDialog(@NonNull Context context) {
            super(context);

            @SuppressLint("InflateParams") LinearLayout layout = (LinearLayout)
                    getLayoutInflater().inflate(R.layout.new_album_dialog, null);

            ImageButton newBtn = layout.findViewById(R.id.new_alubum_button);
            ImageButton cancelBtn = layout.findViewById(R.id.new_album_cancel);
            EditText nameFolder = layout.findViewById(R.id.new_album_name);

            newBtn.setOnClickListener(view -> {
                String newAlbumName = nameFolder.getText().toString();
                if (addNewFolder(newAlbumName)) {
                    dismiss();
                } else {
                    showAlertDialog();
                }
            });

            cancelBtn.setOnClickListener(view -> dismiss());
            setContentView(layout);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(Objects.requireNonNull(getWindow()).getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.dimAmount = 0.7f;
            getWindow().setAttributes(layoutParams);
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Error creating Album").setMessage("Album's name already exists!");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.create().show();
    }

    public static Album favoriteAlbum() {
        for (Album album : albumList) {
            if (album.name.equals(favourite)) {
                return album;
            }
        }
        return null;
    }
}