package com.example.photosGroup3;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    Context context;
    ArrayList<Album> albumList;
    ViewHolder choosingAlbumView = null;
    Album choosingAlbum = null;
    AlbumAdapter adapter = this;

    public AlbumAdapter(ArrayList<Album> albumList, Context context) {
        this.context = context;
        this.albumList = albumList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0){
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        } else {
            holder.albumName.setText(albumList.get(position).name);

            holder.albumImagesCount.setText(String.format(context.getString(R.string.album_image_count), albumList.get(position).imagePaths.size()));
            View.OnClickListener displayAlbum = view -> {
                int pos = holder.getBindingAdapterPosition();
                ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, AlbumDisplayFragment.newInstance(albumList.get(pos)), null)
                        .setReorderingAllowed(true)
                        .commit();
            };

            holder.itemView.setOnClickListener(displayAlbum);
            setBackgroundColor(holder.itemView, null);

            // Nếu là album Favourite
            if (albumList.get(position).name.equals(AlbumsFragment.favourite)) {
                holder.imageView.setImageResource(R.drawable.ic_baseline_favorite_24);
                return;
            } else {
                holder.imageView.setImageResource(R.drawable.ic_baseline_folder_24);
            }

            colorChoosingState(holder.itemView, holder.isChoosing);
            holder.itemView.setOnLongClickListener(view -> {

                holder.isChoosing = !holder.isChoosing;
                colorChoosingState(holder.itemView, holder.isChoosing);
                choosingAlbumView = holder;
                choosingAlbum = albumList.get(holder.getAbsoluteAdapterPosition());
                AlbumOperationDialog dialog = new AlbumOperationDialog(context);
                dialog.show();
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView albumName;
        TextView albumImagesCount;
        LinearLayout albumInfo;
        boolean isChoosing = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.album_image);
            imageView.setImageResource(R.drawable.ic_baseline_folder_24);
            albumName = itemView.findViewById(R.id.album_name);
            albumImagesCount = itemView.findViewById(R.id.album_images_count);
            albumInfo = itemView.findViewById(R.id.album_info);
        }
    }

    public void setBackgroundColor(View view, Integer colorID) {
        if (colorID == null) {
            view.setBackgroundTintList(null);
            return;
        }
        Drawable buttonDrawable = view.getBackground();
        buttonDrawable = DrawableCompat.wrap(buttonDrawable);
        //the color is a direct color int and not a color resource
        DrawableCompat.setTint(buttonDrawable, context.getResources().getColor(colorID));
        view.setBackground(buttonDrawable);
    }

    public void colorChoosingState(View view, boolean isChoosing) {


        int colorWhileChoosing = R.color.fullScreenBtn;
        if (isChoosing) {
            view.setBackgroundResource(R.drawable.custom_row_album);
            setBackgroundColor(view, colorWhileChoosing);
        } else {
            view.setBackgroundResource(android.R.color.transparent);
            setBackgroundColor(view, null);
        }
    }

    private class AlbumOperationDialog extends BottomSheetDialog {
        boolean clearChoosingState = true;

        @Override
        public void dismiss() {
            super.dismiss();
            choosingAlbumView.isChoosing = !choosingAlbumView.isChoosing;
            colorChoosingState(choosingAlbumView.itemView, choosingAlbumView.isChoosing);
            if (clearChoosingState) {
                clearChoosing();
            }

        }

        public AlbumOperationDialog(@NonNull Context context) {
            super(context);
            @SuppressLint("InflateParams") View layout =
                    getLayoutInflater().inflate(R.layout.album_editer, null);

            Button renameBtn = layout.findViewById(R.id.album_rename_option);
            renameBtn.setOnClickListener(view -> {
                RenameAlbumDialog dialog = new RenameAlbumDialog(context);
                dialog.show();
                clearChoosingState = false;
                dismiss();
            });


            Button deleteBtn = layout.findViewById(R.id.album_delete_option);
            deleteBtn.setOnClickListener(view -> {

                ConfirmDeleteAlbumDialog dialog = new ConfirmDeleteAlbumDialog(context, choosingAlbum, () -> {
                    int index = albumList.indexOf(choosingAlbum);
                    albumList.remove(index);
                    adapter.notifyItemRemoved(index);
                    File album = new File(choosingAlbum.path);
                    fileDelete(album);
                    dismiss();
                });
                dialog.show();
            });

            setContentView(layout);
        }
    }

    public class RenameAlbumDialog extends Dialog {
        @Override
        public void dismiss() {
            super.dismiss();
            clearChoosing();
        }

        public RenameAlbumDialog(@NonNull Context context) {
            super(context);
            @SuppressLint("InflateParams") LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.rename_album_dialog, null);
            ImageButton renameBtn = layout.findViewById(R.id.rename_alubum_button);
            ImageButton cancleBtn = layout.findViewById(R.id.rename_album_cancel);
            EditText oldName = layout.findViewById(R.id.old_album_name);
            oldName.setText(choosingAlbum.name);
            EditText newName = layout.findViewById(R.id.rename_album_name);
            renameBtn.setOnClickListener(view -> {
                int index = albumList.indexOf(choosingAlbum);

                String newAlbumName = newName.getText().toString();

                File oldAlbum = new File(choosingAlbum.path);
                String folderPath = Objects.requireNonNull(oldAlbum.getParentFile()).getAbsolutePath();
                File newAlbum = new File(folderPath + "/" + newAlbumName);

                if (newAlbumName.equals(choosingAlbum.name)) {
                    dismiss();
                    return;
                }
                if (newAlbum.isDirectory()) {
                    Toast.makeText(context, "Album's name already used", Toast.LENGTH_SHORT).show();
                    return;
                }
                oldAlbum.renameTo(newAlbum);
                newAlbum = new File(folderPath + "/" + newAlbumName);
                ArrayList<String> imagePath = new ArrayList<>();

                for (File file : Objects.requireNonNull(newAlbum.listFiles())) {
                    if (file.isDirectory()) {

                    } else {
                        for (String extension : MainActivity.ImageExtensions) {

                            if (file.getAbsolutePath().toLowerCase().endsWith(extension)) {
                                imagePath.add(file.getAbsolutePath());
                                break;
                            }

                        }
                    }
                }
                albumList.remove(index);
                albumList.add(index, new Album(newAlbum.getAbsolutePath(), newAlbum.getName(), imagePath));
                adapter.notifyItemChanged(index);
                dismiss();
            });
            cancleBtn.setOnClickListener(view -> dismiss());
            setContentView(layout);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(Objects.requireNonNull(getWindow()).getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes(layoutParams);
        }
    }

    public static class ConfirmDeleteAlbumDialog extends Dialog {
        CallBack callBack;


        public ConfirmDeleteAlbumDialog(@NonNull Context context, Album album, @NonNull CallBack callBack) {
            super(context);
            this.callBack = callBack;

            this.setTitle("Delete confirm");
            this.setContentView(R.layout.delete_album_confirm_dialog);
            Objects.requireNonNull(this.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            String text = (String) ((TextView) this.findViewById(R.id.delete_album_notify)).getText();
            ((TextView) this.findViewById(R.id.delete_album_notify))
                    .setText(text.replace("album_name", album.name));

            this.findViewById(R.id.delete_album_cancel)
                    .setOnClickListener(view -> dismiss());

            this.findViewById(R.id.delete_album_confirm)
                    .setOnClickListener(view -> {
                        callBack.confirmClickedCallback();
                        dismiss();
                    });
        }

        public interface CallBack {
            void confirmClickedCallback();
        }
    }

    private void fileDelete(File file) {
        if (file.isDirectory()) {
            File[] fileInFolder = file.listFiles();
            assert fileInFolder != null;
            for (File fl : fileInFolder) {
                fileDelete(fl);
            }
        }
        if (!file.delete()) {
            Toast.makeText(context, "Delete file fail in AlbumAdapter", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearChoosing() {
        choosingAlbumView = null;
        choosingAlbum = null;
    }
}
