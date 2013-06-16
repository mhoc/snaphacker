package com.mikedhock.snapfind;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    ArrayList<Bitmap> thumbnails;
    ArrayList<String> snapFileList, copiedFileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File destination = new File(Environment.getExternalStorageDirectory().toString() + "/Pictures/SavedSnaps/");
        destination.mkdirs();

        setContentView(R.layout.activity_main);
        refreshSources();

        GridView grid = (GridView) findViewById(R.id.gridview);
        grid.setAdapter(new SnapImageAdapter(this, thumbnails));
        grid.setOnItemClickListener(this);
        grid.setOnItemLongClickListener(this);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    private void refreshSources() {
        refreshSnapFileList();
        copySnaps();
        refreshLocalFileList();
        refreshThumbnailList();

        GridView grid = (GridView) findViewById(R.id.gridview);
        grid.setAdapter(new SnapImageAdapter(this, thumbnails));
        this.getActionBar().setTitle("SnapHax0r 0.4  [" + copiedFileList.size() + "]");
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        File reqImage = new File(Environment.getExternalStorageDirectory().toString() + "/Pictures/SavedSnaps/" + copiedFileList.get(position));
        if (reqImage.exists()) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(reqImage), "image/*");
            startActivity(intent);
        } else {
            Toast.makeText(this, "Requested image could not be found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        File deleteFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/SavedSnaps/" + this.copiedFileList.get(i));
        deleteFile.delete();
        refreshSources();
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Refresh")) {
            refreshSources();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshSnapFileList() {
        ArrayList<String> fileList = new ArrayList<String>();

        try {
            Log.d("DEBUG_MainActivity", "Executing su.");
            Process su = new ProcessBuilder().command("su").redirectErrorStream(true).start();

            Log.d("DEBUG_MainActivity", "Opening I/O Streams.");
            InputStream su_input = su.getInputStream();
            OutputStream su_output = su.getOutputStream();
            BufferedReader su_input_buffer = new BufferedReader(new InputStreamReader(su_input));

            Log.d("DEBUG_MainActivity", "Executing superuser ls command.");
            byte[] command = "ls /data/data/com.snapchat.android/cache/received_image_snaps/\n".getBytes();
            su_output.write(command);
            su_output.write("exit\n".getBytes());
            su_output.flush();

            Log.d("DEBUG_MainActivity", "Waiting for su process to finish.");
            //su.waitFor();

            Log.d("DEBUG_MainActivity", "Reading from stream output.");
            String buffer;
            StringBuilder result = new StringBuilder();
            while ((buffer = su_input_buffer.readLine()) != null) {
                Log.d("DEBUG_MainActivity", "Output : " + buffer);
                fileList.add(buffer);
            }

        } catch (Exception e) {
            Log.d("DEBUG_MainActivity", "Exception caught. See stack trace.");
            e.printStackTrace();
        }

        this.snapFileList = fileList;
    }

    private void refreshLocalFileList() {
        ArrayList<String> fileList = new ArrayList<String>();

        File path = new File(Environment.getExternalStorageDirectory().toString() + "/Pictures/SavedSnaps/");

        if (path.exists()) {
            for (int i = 0; i < path.list().length; i++) {
                fileList.add(path.list()[i]);
            }
        } else {
            Toast.makeText(this, "A grievous error has occurred. Abandon ship.", Toast.LENGTH_SHORT).show();
        }
        this.copiedFileList = fileList;
    }

    private void refreshThumbnailList() {
        ArrayList<Bitmap> thumbnails = new ArrayList<Bitmap>();
        for (int i = 0; i < copiedFileList.size(); i++) {
            thumbnails.add(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + "/Pictures/SavedSnaps/" + copiedFileList.get(i)));
        }
        this.thumbnails = thumbnails;
    }

    private void copySnaps() {
        Random rand = new Random();
        try {
            Process su = new ProcessBuilder().command("su").redirectErrorStream(true).start();
            InputStream su_input = su.getInputStream();
            OutputStream su_output = su.getOutputStream();
            BufferedReader su_input_buffer = new BufferedReader(new InputStreamReader(su_input));

            for (String file : this.snapFileList) {
                String command = "dd if=/data/data/com.snapchat.android/cache/received_image_snaps/" + file + " of=" +
                        Environment.getExternalStorageDirectory().toString() + "/Pictures/SavedSnaps/" + rand.nextLong() + ".jpg\n";
                Log.d("DEBUG_MainActivity", command);
                su_output.write(command.getBytes());
            }

            su_output.write("exit\n".getBytes());
            su_output.flush();

            String buffer;
            while ((buffer = su_input_buffer.readLine()) != null) {
                Log.d("DEBUG_MainActivity", "COPY RESULT: " + buffer);
            }

        } catch (Exception e) {

        }

        checkLocalsForDups();

    }

    private void checkLocalsForDups() {
        refreshLocalFileList();

        ArrayList<String> hashes = new ArrayList<String>();

        for (String fileName : this.copiedFileList) {
            File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/SavedSnaps/" + fileName);

            try {
                if (file.exists()) {
                    String md5 = Files.hash(file, Hashing.md5()).toString();
                    if (!(hashes.contains(md5))) {
                        hashes.add(md5);
                    } else {
                        file.delete();
                    }
                }
            } catch (IOException e) {

            }
        }
    }

}
