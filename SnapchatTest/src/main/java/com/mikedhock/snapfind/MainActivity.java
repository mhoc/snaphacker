package com.mikedhock.snapfind;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    SourceManager sources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File destination = new File(SourceManager.localPathPrefix);
        destination.mkdirs();

        setContentView(R.layout.activity_main);
        GridView grid = (GridView) findViewById(R.id.gridview);
        grid.setOnItemClickListener(this);
        grid.setOnItemLongClickListener(this);

        sources = new SourceManager(this, grid);
        sources.refresh();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_settings, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        File reqImage = new File(sources.localPathPrefix + sources.localFileList.get(position));
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
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
        final int position = pos;
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete Image?")
                .setMessage("This will (probably) permanently delete the image from your phone.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File toDelete = new File(SourceManager.localPathPrefix + sources.localFileList.get(position));
                        toDelete.delete();
                        dialogInterface.dismiss();
                        sources.refresh();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Refresh")) {
            sources.refresh();
        }
        return super.onOptionsItemSelected(item);
    }

}
