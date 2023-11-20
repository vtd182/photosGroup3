package com.example.photosGroup3.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photosGroup3.R;
import com.example.photosGroup3.viewPagerItem;

import java.util.ArrayList;

public class viewPagerAdapterForSlider extends RecyclerView.Adapter<viewPagerAdapterForSlider.ViewHolder> {

    ArrayList<viewPagerItem> arrayItems;
    SlideShow main;

    public viewPagerAdapterForSlider(ArrayList<viewPagerItem> arrayItems , SlideShow main) {
        this.main=main;
        this.arrayItems = arrayItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.slider_picture,parent,false);
//                .inflate(R.layout.full_creen_picture,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        viewPagerItem item= arrayItems.get(position);
        holder.img.setImageBitmap(item.getItemBitmap());
    }


    @Override
    public int getItemCount() {
        return arrayItems.size();
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
        ImageView img;
        //    TextView txtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            img=itemView.findViewById(R.id.sliderImage);
//            img=itemView.findViewById(R.id.imageView);
            // txtName=itemView.findViewById(R.id.tvName);

        }
    }
}
