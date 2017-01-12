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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by flywolf on 3/5/15.
 */
public class OpenPhoto extends Fragment {
    final String LOG_TAG = "myLogs";

    public interface SelectPhotoListener {
        void onFinishSelectPhoto(String inputText);
    }

    private DbWorker.Relative openInDialog;
    private View dialogView;
    private ImageView leaf;
    private Context context;

    public OpenPhoto(DbWorker.Relative openInDialog, View dialogView, ImageView leaf, Context context) {
        this.openInDialog = openInDialog;
        this.dialogView = dialogView;
        this.leaf = leaf;
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(LOG_TAG, "Open Fragment");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                1);

    /*    String file = FamilyTree.extStorageDirectory+"fromCamera"+openInDialog.getId()+".png";
        File newfile = new File(file);
        try {
            newfile.createNewFile();
        } catch (IOException e) {}

        Uri outputFileUri = Uri.fromFile(newfile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(cameraIntent, 0);
*/

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //  super.onActivityResult(1, 2, data);
        Log.d(LOG_TAG, "Photo selected");
        if (data != null && data.getData() != null) {
            Uri _uri = data.getData();
            Bitmap bm = readBitmap(_uri);
            Drawable bgrImage = new BitmapDrawable(bm);
             try {
                openInDialog.setImgB(DbWorker.getBytes(bm));//PhotoUrl(FamilyTree.savePhoto(img, openInDialog));
                Log.d(LOG_TAG, "read photo1 " + openInDialog.getImgB());
            } catch (Exception e) {
                Log.e(LOG_TAG, "read photo error " + e.getMessage());
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            leaf.setImageDrawable(bgrImage);
            ImageView photo = (ImageView) dialogView
                    .findViewById(R.id.big_photo);
            photo.setImageDrawable(bgrImage);
            FamilyTree.dbWorker.saveRelative(openInDialog);
        }
        super.onActivityResult(requestCode, resultCode, data);

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