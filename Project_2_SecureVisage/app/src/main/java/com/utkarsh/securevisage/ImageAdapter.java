package com.utkarsh.securevisage;

import android.graphics.Bitmap;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private ArrayList<Pair<String, Bitmap>> imagesList;
    private OnImageLongClickListener onImageLongClickListener;

    public ImageAdapter(ArrayList<Pair<String, Bitmap>> imagesList) {
        this.imagesList = imagesList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Pair<String, Bitmap> imagePair = imagesList.get(position);
        Bitmap imageBitmap = imagePair.second;
        holder.imageView.setImageBitmap(imageBitmap);

        // Set a long-press listener for each image item
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onImageLongClickListener != null) {
                    onImageLongClickListener.onImageLongClick(imagePair.first, imageBitmap);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public interface OnImageLongClickListener {
        void onImageLongClick(String filename, Bitmap imageBitmap);
    }

    public void setOnImageLongClickListener(OnImageLongClickListener listener) {
        this.onImageLongClickListener = listener;
    }
}
