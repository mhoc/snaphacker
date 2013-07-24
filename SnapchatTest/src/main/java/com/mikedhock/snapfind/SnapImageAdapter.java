package com.mikedhock.snapfind;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

public class SnapImageAdapter extends BaseAdapter {

    Context context;
    GridView.LayoutParams layoutParams = new GridView.LayoutParams(360,640);
    ArrayList<Bitmap> bitmaps;

    public SnapImageAdapter(Context context, ArrayList<Snap> snapArray) {
        this.context = context;

        bitmaps = new ArrayList<Bitmap>();
        for (Snap s : snapArray) {
            bitmaps.add(s.getCompressedImage());
        }
    }

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int i) {
        return 0;
    }

    public int getCount() {
        return bitmaps.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv;

        if (convertView == null) {
            iv = new ImageView(context);
            iv.setLayoutParams(layoutParams);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setPadding(8,8,8,8);
        } else {
            iv = (ImageView) convertView;
        }

        iv.setImageBitmap(bitmaps.get(position));
        return iv;
    }
}
