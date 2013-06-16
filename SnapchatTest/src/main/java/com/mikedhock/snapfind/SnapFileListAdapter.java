package com.mikedhock.snapfind;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class SnapFileListAdapter extends ArrayAdapter {

    Context context;
    ArrayList<String> fileList;
    ArrayList<Bitmap> thumbs;

    public SnapFileListAdapter(Context context, ArrayList<String> values, ArrayList<Bitmap> thumbs) {
        super(context, R.layout.snap_list_item, values);
        this.context = context;
        this.fileList = values;
        this.thumbs = thumbs;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.snap_list_item, parent, false);

        ImageView iv = (ImageView) rowView.findViewById(R.id.snap_list_item_image);
        TextView tv = (TextView) rowView.findViewById(R.id.snap_list_item_text);

        String filename = fileList.get(position);
        tv.setText(filename);

        iv.setImageBitmap(thumbs.get(position));

        return rowView;
    }

}
