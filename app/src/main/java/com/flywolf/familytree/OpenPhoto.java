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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
            String realPath;
            // SDK < API11
            if (Build.VERSION.SDK_INT < 11)
                realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(context, data.getData());

                // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19)
                realPath = RealPathUtil.getRealPathFromURI_API11to18(context, data.getData());

                // SDK > 19 (Android 4.4)
            else
                realPath = RealPathUtil.getRealPathFromURI_API19(context, data.getData());

            Log.d(LOG_TAG, "photo saved try _uri=" + realPath);
            try {
                //Picasso.with(context).load(R.drawable.photo).into(leaf);
                //FamilyTree.savePhoto(Picasso.with(context).load("file://"+realPath).resize(300, 300).get(),openInDialog);
                FamilyTree.savePhoto(FamilyTree.decodeFile(realPath), openInDialog);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            leaf.setImageDrawable(bgrImage);
            ImageView photo = (ImageView) dialogView
                    .findViewById(R.id.big_photo);
            photo.setImageDrawable(bgrImage);
            openInDialog.setPhotoUrl(realPath);
            // File myFile = new File(_uri.toString());
            FamilyTree.dbWorker.saveRelative(openInDialog);
            Log.d(LOG_TAG, "path = " + realPath);
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