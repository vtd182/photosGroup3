package com.example.photosGroup3;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> implements Filterable {

    private final ImageDisplay imageDisplay;
    private final Context context;
    private boolean isGrid;
    private final ArrayList<String> imagePhotos;
    private ArrayList<String> filteredList;

    public ListAdapter(ImageDisplay imageDisplay, ArrayList<String> imagePhotos
            , boolean isGrid, Context context) {
        this.imageDisplay = imageDisplay;
        this.imagePhotos = imagePhotos;
        this.filteredList = imagePhotos;
        this.isGrid = isGrid;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);

            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ListViewHolder(view);
        }
    }

    public void setGrid(boolean isGrid) {
        this.isGrid = isGrid;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemViewType(int position) {
        // Return the view type based on your logic
        if (isGrid)
            return 1;
        else
            return 0;
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase();
                FilterResults filterResults = new FilterResults();
                ArrayList<String> filteredList = new ArrayList<>();

                for (String data : imagePhotos) {
                    // Use FuzzyWuzzy to calculate similarity ratio
                    File imgFile = new File(data);
                    Date imgDate = new Date(imgFile.lastModified());
                    int nameRatio = FuzzySearch.ratio(charString, imgFile.getName().toLowerCase());
                    int dateRatio = FuzzySearch.ratio(charString, imgDate.toString().toLowerCase());

                    // You can set a threshold for similarity ratio
                    if (nameRatio > 15 || dateRatio > 15) { // You can adjust the threshold as needed
                        filteredList.add(data);
                    }
                }

                filterResults.values = filteredList;
                filterResults.count = filteredList.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredList = (ArrayList<String>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected final ImageView imageView;
        protected final CheckBox check;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            check = itemView.findViewById(R.id.checkImage);
            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, SelectedPicture.class);
                intent.putExtra("pos", getAdapterPosition());
                intent.putExtra("images", filteredList);
                context.startActivity(intent);
            });
            itemView.setOnLongClickListener(view -> {
                        ImageDisplay.getInstance().isHolding = true;
                        ((MainActivity) context).Holding(true);
                        String selectedName = ImageDisplay.getInstance().images.get(getAdapterPosition());

                        ImageDisplay.getInstance().selectedImages = ((MainActivity) context).
                                adjustChooseToDeleteInList(selectedName, "choose");
                        ((MainActivity) context).SelectedTextChange();

                        imageDisplay.notifyChangeGridLayout();
                        return true;
                    }
            );
        }

        public void bind(int position) {
            if (imageDisplay.isHolding) {
                check.setVisibility(View.VISIBLE);

                check.setChecked(imageDisplay.selectedImages.contains(filteredList.get(position)));

                check.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (compoundButton.isPressed()) {
                        if (position < filteredList.size()) {

                            if (b) {
                                if (!imageDisplay.selectedImages.contains(filteredList.get(position))) {
                                    imageDisplay.selectedImages = ((MainActivity) imageDisplay.requireContext()).
                                            adjustChooseToDeleteInList(filteredList.get(position), "choose");
                                }

                            } else {
                                if (imageDisplay.selectedImages.contains(filteredList.get(position))) {
                                    imageDisplay.selectedImages = ((MainActivity) imageDisplay.requireContext()).
                                            adjustChooseToDeleteInList(filteredList.get(position), "unchoose");
                                }
                            }
                            ((MainActivity) imageDisplay.requireContext()).SelectedTextChange();
                            imageDisplay.notifyChangeGridLayout();

                        }
                    }
                });
            } else {
                check.setVisibility(View.INVISIBLE);
            }

            File imgFile = new File(filteredList.get(position));
            Glide.with(imageDisplay.context)
                    .load(Uri.parse("file://" + imgFile.getAbsolutePath()))
                    .into(imageView);
        }

    }

    public class ListViewHolder extends ListAdapter.ViewHolder {
        private final TextView name;
        private final TextView date;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            date = itemView.findViewById(R.id.tvDate);
        }

        public void bind(int position) {
            super.bind(position);
            File imgFile = new File(filteredList.get(position));
            Date imgDate = new Date(imgFile.lastModified());
            name.setText(imgFile.getName());
            date.setText(imgDate.toString());
        }
    }
}

