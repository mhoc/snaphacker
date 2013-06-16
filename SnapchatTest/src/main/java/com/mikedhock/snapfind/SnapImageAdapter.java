package com.mikedhock.snapfind;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

public class SnapImageAdapter extends BaseAdapter {

    ArrayList<Bitmap> images;
    Context context;

    public SnapImageAdapter(Context context, ArrayList<Bitmap> images) {
        this.images = images;
        this.context = context;
    }

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int i) {
        return 0;
    }

    public int getCount() {
        return images.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv;

        if (convertView == null) {
            iv = new ImageView(context);
            iv.setLayoutParams(new GridView.LayoutParams(205,366));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setPadding(8,8,8,8);
        } else {
            iv = (ImageView) convertView;
        }

        iv.setImageBitmap(images.get(position));
        return iv;
    }
}
