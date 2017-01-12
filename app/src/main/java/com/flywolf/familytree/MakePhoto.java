package com.flywolf.familytree;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
    public MakePhoto(){}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_PHOTO_CODE);
        }
    }

    public void setOpenInDialog(DbWorker.Relative openInDialog) {
        this.openInDialog = openInDialog;
    }

    public void setDialogView(View dialogView) {
        this.dialogView = dialogView;
    }

    public void setLeaf(ImageView leaf) {
        this.leaf = leaf;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "Photo selected=" + resultCode);
        if (requestCode == TAKE_PHOTO_CODE && resultCode == -1) {
            Bundle extras = data.getExtras();
            Bitmap bgrImage = (Bitmap) extras.get("data");
            try {
                openInDialog.setImgB(DbWorker.getBytes(bgrImage));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            FamilyTree.dbWorker.saveRelative(openInDialog);
            leaf.setImageBitmap(bgrImage);
            ImageView photo = (ImageView) dialogView
                    .findViewById(R.id.big_photo);
            photo.setImageBitmap(bgrImage);
     }
        super.onActivityResult(requestCode, resultCode, data);

    }

}