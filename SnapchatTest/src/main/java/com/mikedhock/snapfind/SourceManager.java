package com.mikedhock.snapfind;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.GridView;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class SourceManager {

    ArrayList<Snap> rootSnapList;
    ArrayList<Snap> localSnapList;
    Context context;
    GridView view;

    public static final String localPathPrefix = Environment.getExternalStorageDirectory().toString() + "/Pictures/SavedSnaps/";

    /**
     * @param context Pretty standard, just your base context.
     * @param viewToUpdate The GridView to update every refresh.
     */
    public SourceManager(Context context, GridView viewToUpdate) {
        this.context = context;
        this.view = viewToUpdate;
    }

    /** Runs a full refresh of all the data in the application.
     *  Resets the public fields of the object with appropriate data.
     *  Does a full copy of the files from the snap dir to the local dir.
     *  This method is threaded to run in the background, as it is very resource intensive.
     */
    public void refresh() {
        new Thread(new Runnable() {
            public void run() {
                rootSnapList = getRootSnapList();
                copySnaps(rootSnapList);
                localSnapList = getLocalSnapList();

                view.post(new Runnable() {
                   public void run() {
                       view.setAdapter(new SnapImageAdapter(context, localSnapList));
                   }
                });
            }
        }).start();
    }

    /** Retrieves a list of all the images Snapchat has in its directory and returns it as an Array */
    private ArrayList<Snap> getRootSnapList() {
        ArrayList<Snap> fileList = new ArrayList<Snap>();

        try {
            // Create the SU process and open streams
            Process su = new ProcessBuilder().command("su").redirectErrorStream(true).start();

            InputStream su_input = su.getInputStream();
            OutputStream su_output = su.getOutputStream();
            BufferedReader su_input_buffer = new BufferedReader(new InputStreamReader(su_input));

            // This command will return ONLY the filenames and the date the file was created.
            byte[] command = "ls -l /data/data/com.snapchat.android/cache/received_image_snaps/ | awk \'{print $5,$6,$7}\'\n".getBytes();
            su_output.write(command);
            su_output.flush();
            su_output.write("exit\n".getBytes());

            String buffer;
            StringBuilder result = new StringBuilder();
            while ((buffer = su_input_buffer.readLine()) != null) {
                Log.d("QWERTY", buffer);
                String[] lineSplit = buffer.split(" ");
                String[] dateResult = lineSplit[0].split("-");
                String[] timeResult = lineSplit[1].split(":");

                GregorianCalendar receiveDate = new GregorianCalendar();
                receiveDate.set(Integer.parseInt(dateResult[0]), Integer.parseInt(dateResult[1]),
                        Integer.parseInt(dateResult[2]), Integer.parseInt(timeResult[0]),
                        Integer.parseInt(timeResult[1]), 0);

                Snap current = new Snap(lineSplit[2], receiveDate);
                fileList.add(current);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileList;
    }

    /** Copies all of the images from the Snapchat directory into the local SD card directory
     *  rootSnapList should be an arraylist of all the filenames in the snap directory, and can be
     *  obtained with getRootSnapList();
     */
    private boolean copySnaps(ArrayList<Snap> snapFileList) {
        try {
            // I've tried re-using the SU process from above, but I couldn't get it to
            // handle the output stream properly. So a new one is created here.
            Process su = new ProcessBuilder().command("su").redirectErrorStream(true).start();
            InputStream su_input = su.getInputStream();
            OutputStream su_output = su.getOutputStream();
            BufferedReader su_input_buffer = new BufferedReader(new InputStreamReader(su_input));

            // The files are copied to the sdcard using the dd command, as root privledge is required.
            // They are renamed by the date in which the user recieved them.
            // This way, they will remain in order when the user refreshes the app.
            for (Snap file : snapFileList) {
                String command = "dd if=/data/data/com.snapchat.android/cache/received_image_snaps/" +
                        file.getFileName() + " of=" + this.localPathPrefix + file.getDateInMillis() + ".jpg\n";
                su_output.write(command.getBytes());
            }

            su_output.write("exit\n".getBytes());

        } catch (Exception e) {

        }

        return true;
    }

    /** Returns a list of all the files in the local image storage.
     *  Automatically checks and removes all duplicates while its doing its thing. */
    private ArrayList<Snap> getLocalSnapList() {
        ArrayList<Snap> fileList = new ArrayList<Snap>();
        ArrayList<String> hashes = new ArrayList<String>();
        File path = new File(this.localPathPrefix);

        if (path.exists()) {
            // For each file in the directory
            for (File f : path.listFiles()) {
                try {
                    // Run its MD5 and make sure it hasn't already been discovered
                    String md5 = Files.hash(f, Hashing.md5()).toString();
                    if (!(hashes.contains(md5))) {
                        // Assuming it has a unique MD5, create the localsnap and add it
                        hashes.add(md5);
                        Snap s = new Snap(f.getName(), BitmapFactory.decodeFile(this.localPathPrefix + f));
                        fileList.add(s);
                    } else {
                        f.delete();
                    }
                } catch (IOException e) {}
            }
        }

        return fileList;
    }

}
