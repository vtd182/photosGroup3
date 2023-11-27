package com.example.photosGroup3;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlbumDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

// TO DO: add new toolbar for this fragment
public class AlbumDisplayFragment extends Fragment implements ImageDisplay.LongClickCallback {
    Context context;
    ImageButton back_button, resize_button;
    TextView album_name, album_images_count;
    Album album;
    FloatingActionButton add_images;
    TableLayout header;
    ArrayList<String> addedPaths = new ArrayList<>();

    public AlbumDisplayFragment() {
        // Required empty public constructor
    }


    public static AlbumDisplayFragment newInstance(Album album) {
        AlbumDisplayFragment fragment = new AlbumDisplayFragment();
        fragment.album = album;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get args
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        CoordinatorLayout layout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_album_display, container, false);

        header = layout.findViewById(R.id.album_header);

        back_button = layout.findViewById(R.id.album_display_back);
        back_button.setOnClickListener(view -> ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, AlbumsFragment.getInstance(), null)
                .setReorderingAllowed(true)
                .commit());

        album_name = layout.findViewById(R.id.album_display_name);
        album_name.setText(album.name);

        album_images_count = layout.findViewById(R.id.album_images_count3);
        album_images_count.setText(String.format(context.getString(R.string.album_image_count), album.imagePaths.size()));


        add_images = layout.findViewById(R.id.add_image);
        add_images.setOnClickListener(view -> {
            ImageChoosingDialog dialog = new ImageChoosingDialog(context);
            dialog.show();
        });


        ImageDisplay.changeINSTANCE();
        ImageDisplay instance = ImageDisplay.getInstance();
        instance.setImagesData(album.imagePaths);
        ImageDisplay.getInstance().setLongClickCallBack(this);

        getChildFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.album_display_list, ImageDisplay.getInstance(), null)
                .commit();

        resize_button = layout.findViewById(R.id.resizeBtn);
        resize_button.setOnClickListener(view -> {
            ImageDisplay.getInstance().numCol = ImageDisplay.getInstance().numCol % 5 + 1;
            if (ImageDisplay.getInstance().numCol == 1) {
//                    numCol=2;
                ImageDisplay.getInstance().gridView.setAdapter(ImageDisplay.getInstance().listAdapter);

            } else if (ImageDisplay.getInstance().numCol == 2) {
                ImageDisplay.getInstance().gridView.setAdapter(ImageDisplay.getInstance().customAdapter);
            }
            ImageDisplay.getInstance().gridView.setNumColumns(ImageDisplay.getInstance().numCol);
        });
        return layout;
    }

    @Override
    public void onDestroyView() {
        ImageDisplay.restoreINSTANCE();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        ImageDisplay.getInstance().getToolbar().setVisibility(View.GONE);
    }

    @Override
    public void onLongClick() {
        ViewGroup.LayoutParams params = header.getLayoutParams();
        params.height = (int) (60 * getResources().getDisplayMetrics().density);
        header.setLayoutParams(params);
    }

    @Override
    public void afterLongClick() {
        ViewGroup.LayoutParams params = header.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        header.setLayoutParams(params);
        album_images_count.setText(String.format(context.getString(R.string.album_image_count), album.imagePaths.size()));
    }


    private class ImageChoosingDialog extends Dialog {
        public ImageChoosingDialog(@NonNull Context context) {
            super(context);
            addedPaths.clear();
            @SuppressLint("InflateParams") RelativeLayout layout =
                    (RelativeLayout) getLayoutInflater().inflate(R.layout.image_choosing_bar, null);

            GridView imageList = layout.findViewById(R.id.image_choosing_imageList);
            imageList.setAdapter(new ImageChoosingAdapter(((MainActivity) context).getFileinDir()));


            ImageButton add_btn = layout.findViewById(R.id.image_choosing_add);
            add_btn.setOnClickListener(view -> {
                dismiss();
                MoveOrCopyForDialog.MoveOrCopyCallBack callBack = new MoveOrCopyForDialog.MoveOrCopyCallBack() {
                    @Override
                    public void dismissCallback(String method) {
                        album_images_count.setText(String.format(context.getString(R.string.album_image_count), album.imagePaths.size()));
                    }

                    @Override
                    public void copiedCallback(String newImagePath) {
                        ImageDisplay.getInstance().addNewImage(newImagePath, 1);
                    }

                    @Override
                    public void removedCallback(String oldImagePath, String newImagePath) {
                        ((MainActivity) context).FileInPaths.remove(oldImagePath);
                        ImageDisplay.getInstance().addNewImage(newImagePath, 1);
                    }
                };
                MoveOrCopyForDialog dialog = new MoveOrCopyForDialog(context, callBack, album, addedPaths);
                dialog.show();
            });

            ImageButton cancel_btn = layout.findViewById(R.id.image_choosing_cancel);
            cancel_btn.setOnClickListener(view -> dismiss());

            setContentView(layout);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(Objects.requireNonNull(getWindow()).getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            getWindow().setAttributes(layoutParams);
        }

        private class ImageChoosingAdapter extends BaseAdapter {
            ArrayList<String> allImagePaths;
            ArrayList<Boolean> checkBoxValues;

            public ImageChoosingAdapter(ArrayList<String> allImagePaths) {
                this.allImagePaths = allImagePaths;

                checkBoxValues = new ArrayList<>();
                for (int i = 0; i < allImagePaths.size(); i++) {
                    checkBoxValues.add(false);
                }
            }

            @Override
            public int getCount() {
                return allImagePaths.size();
            }

            @Override
            public Object getItem(int i) {
                return allImagePaths.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                ViewHolder viewHolder;
//            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.row_item_with_choose, viewGroup, false);
                    viewHolder = new ViewHolder();
                    viewHolder.imageView = view.findViewById(R.id.image_to_choose);
                    viewHolder.checkBox = view.findViewById(R.id.image_check_box);
                    viewHolder.checkBox.setChecked(checkBoxValues.get(i));
                    view.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) view.getTag();
                }

                view.setOnClickListener(view1 -> {
                    ViewHolder viewHolder1 = (ViewHolder) view1.getTag();
                    if (viewHolder1.checkBox.isChecked()) {
                        checkBoxValues.remove(i);
                        checkBoxValues.add(i, false);
                        viewHolder1.checkBox.setChecked(checkBoxValues.get(i));
                        addedPaths.remove(allImagePaths.get(i));
                    } else {
                        checkBoxValues.remove(i);
                        checkBoxValues.add(i, true);
                        viewHolder1.checkBox.setChecked(checkBoxValues.get(i));
                        addedPaths.add(allImagePaths.get(i));
                    }
                });

                viewHolder.checkBox.setChecked(checkBoxValues.get(i));
                File imgFile = new File(allImagePaths.get(i));
                ImageLoader.getInstance().displayImage(String.valueOf(
                        Uri.parse("file://" + imgFile.getAbsolutePath())), viewHolder.imageView);
                return view;
            }

            private class ViewHolder {
                ImageView imageView;
                CheckBox checkBox;
            }
        }
    }

}