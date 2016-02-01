package com.flywolf.familytree;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by flywolf on 3/5/15.
 */
public class MakePhoto extends Fragment {
    final String LOG_TAG = "myLogs";

    public interface SelectPhotoListener {
        void onFinishSelectPhoto(String inputText);
    }

    private DbWorker.Relative openInDialog;
    private View dialogView;
    private ImageView leaf;
    private Context context;
    int TAKE_PHOTO_CODE = 0;
    String file;

    public MakePhoto(DbWorker.Relative openInDialog, View dialogView, ImageView leaf, Context context) {
        this.openInDialog = openInDialog;
        this.dialogView = dialogView;
        this.leaf = leaf;
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        file = FamilyTree.extStorageDirectory + "fromCamera" + openInDialog.getId() + ".png";
        File newfile = new File(file);
        try {
            newfile.createNewFile();
        } catch (IOException e) {
        }

        Uri outputFileUri = Uri.fromFile(newfile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //  super.onActivityResult(1, 2, data);
        Log.d(LOG_TAG, "Photo selected=" + resultCode);
        if (requestCode == TAKE_PHOTO_CODE && resultCode == -1) {
            try {

                //FamilyTree.savePhoto(Picasso.with(context).load(file).resize(300, 300).get(), openInDialog);
                FamilyTree.savePhoto(FamilyTree.decodeFile(file), openInDialog);
                Log.d(LOG_TAG, "Photo saved");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            openInDialog.setPhotoUrl(file);
            FamilyTree.dbWorker.saveRelative(openInDialog);
            Bitmap bgrImage = null;
            try {
                bgrImage = readPhoto(openInDialog);
            } catch (Exception e) {
                e.printStackTrace();
            }
            leaf.setImageBitmap(bgrImage);
            ImageView photo = (ImageView) dialogView
                    .findViewById(R.id.big_photo);
            photo.setImageBitmap(bgrImage);

            Log.d(LOG_TAG, "path = " + file);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private Bitmap readPhoto(DbWorker.Relative r) throws Exception {
        Bitmap bm = null;
        try {

            //bm = Picasso.with(context).load(FamilyTree.extStorageDirectory + "/familytree" + r.getLeafId() + ".png").get();
            //FamilyTree.decodeFile(FamilyTree.extStorageDirectory + "/familytree" + r.getLeafId()
            //        + ".png");
            bm = FamilyTree.decodeFile(FamilyTree.extStorageDirectory + "/familytree" + r.getLeafId()
                    + ".png");
        } catch (Exception e) {
            Log.d(LOG_TAG, "error read leaf " + e.toString());
            // bm =  Picasso.with(context).load(r.photoUrl).get();
            bm = FamilyTree.decodeFile(r.photoUrl);
        }
        return bm;
    }

    public Bitmap readBitmap(Uri selectedImage) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2; // reduce quality
        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(
                    selectedImage, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                bm = BitmapFactory.decodeFileDescriptor(
                        fileDescriptor.getFileDescriptor(), null, options);
                fileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bm;
    }


}