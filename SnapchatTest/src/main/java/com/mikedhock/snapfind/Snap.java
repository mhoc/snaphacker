package com.mikedhock.snapfind;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.util.GregorianCalendar;

/** Object which represents a single Snap from a user. */
public class Snap {

    /* Bitmaps of the image itself, compressed and uncompressed */
    private Bitmap originalImage, compressedImage;

    /* Date the snap was received on, according to the snap image directory */
    private GregorianCalendar date;

    /* The file name, with the .nomedia extension */
    private String fileName;

    /** For creating a Snap which is local. Date doesn't matter, but the bitmap does. */
    public Snap(String fileName, Bitmap originalImage) {
        this.fileName = fileName;
        this.originalImage = originalImage;
        this.date = null;
        compressImage(0.5f);
    }

    /** For creating a Snap which is root. Date is important, but the bitmap isn't. */
    public Snap(String fileName, GregorianCalendar date) {
        this.fileName = fileName;
        this.originalImage = null;
        this.date = date;
    }

    /** Compresses the image stored in original image by a factor passed in as the argument. */
    private void compressImage(float factor) {
        if (this.originalImage != null) {
            int width = this.originalImage.getWidth();
            int height = this.originalImage.getHeight();
            Matrix matrix = new Matrix();
            matrix.postScale(factor, factor);
            Bitmap compressed = Bitmap.createBitmap(originalImage, 0, 0, width, height, matrix, false);
            this.compressedImage = compressed;
        } else {
            this.compressedImage = null;
        }
    }

    /** Returns the original image associated with this Snap.
     *  Might return null if you request an image on a snap which is in the root directory. */
    public Bitmap getImage() {
        return originalImage;
    }

    /** Returns the compressed image associated with this smap.
     *  Might return null if you request an image on a snap which is in the root directory. */
    public Bitmap getCompressedImage() {
        return compressedImage;
    }

    /** Returns the date associated with this snap.
     *  Will return -1 if this is a snap for which the date logically makes no sense
     *  As in, it is in the local directory. */
    public long getDateInMillis() {
        if (date != null) {
            return date.getTimeInMillis();
        } else {
            return -1;
        }
    }

    /** Returns the file name of the Snap */
    public String getFileName() {
        return fileName;
    }

}
