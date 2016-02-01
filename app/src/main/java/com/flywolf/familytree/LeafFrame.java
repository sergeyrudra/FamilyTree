package com.flywolf.familytree;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by flywolf on 2/26/15.
 */
public class LeafFrame extends RelativeLayout {
    private ImageView leaf;
    private ImageView leafFrame;
    private TextView leafBirthday;
    private TextView leafText;
    private DbWorker.Relative openInDialog;
    int IMAGE_MAX_SIZE = 300;
    private final String extStorageDirectory = Environment
            .getExternalStorageDirectory().toString() + "/familytree";
    final String LOG_TAG = "myLogs";
    private Context context;
    private FragmentManager fragmentManager;

    public LeafFrame(Context context) {
        super(context);
        initComponent();
    }

    private void initComponent() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.leaf_layout, this);
        leaf = (ImageView) findViewById(R.id.leaf);
        //Picasso.with(getContext()).load(R.drawable.photo).into(leaf);
        leaf.setOnClickListener(buttonListener);
        leafFrame = (ImageView) findViewById(R.id.leafFrame);
        leafBirthday = (TextView) findViewById(R.id.leafBirthday);
        leafText = (TextView) findViewById(R.id.leafText);
    }

    public void setLeaf(int resourceId) {
        this.leaf.setImageResource(resourceId);
    }

    /* public void setLeafBitmap(Bitmap b) {
         this.leaf.setImageBitmap(b);
     }*/
    public void setLeaf(String url) {
        Picasso.with(getContext()).load(url).into(this.leaf);
    }


    public void setLeafFrame(int resourceId) {
        this.leafFrame.setImageResource(resourceId);
        ;
    }

    public void setContext(Context context) {
        this.context = context;
        ;
    }

    public DbWorker.Relative getOpenInDialog() {
        return openInDialog;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        ;
    }

    public void setOpenInDialog(DbWorker.Relative openInDialog) {
        this.openInDialog = openInDialog;
    }

    public void setLeafId(int id) {
        this.leaf.setId(100 + id);

    }

    public void setLeafBirthday(String text) {
        this.leafBirthday.setText(text);
    }

    public void setLeafBirthdayVisibility(boolean visible) {
        this.leafBirthday.setVisibility(visible ? android.view.View.VISIBLE : View.INVISIBLE);
    }

    public void setLeafTextVisibility(boolean visible) {
        this.leafText.setVisibility(visible ? android.view.View.VISIBLE : View.INVISIBLE);
    }


    private View dialogView;
    private final OnClickListener buttonListener = new OnClickListener() {
        public void onClick(View view) {
            final AlertDialog dialog = new AlertDialog.Builder(context)
                    .create();
            LayoutInflater inflater = LayoutInflater.from(context);
            dialog.setTitle(getContext().getString(R.string.openn_dialog_title));
            final View dialogView = inflater.inflate(R.layout.open_photo, null); // xml Layout
            // file for
            // imageView
            ImageView img = (ImageView) dialogView.findViewById(R.id.big_photo);
            if (openInDialog.photoUrl != null
                    && !openInDialog.photoUrl.contentEquals("")) {
                Bitmap bgrImage;
                try {
                    IMAGE_MAX_SIZE = 350;
                    bgrImage = readPhoto(openInDialog);
                    img.setImageBitmap(bgrImage);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {

                img = (ImageView) dialogView.findViewById(R.id.big_photo);
                img.setImageResource((openInDialog.leafId % 2 != 0) ? R.drawable.leaf2w
                        : R.drawable.leaf2m);

            }
            int frameId = FamilyTree.selectFrame(openInDialog);
            img = (ImageView) dialogView.findViewById(R.id.big_photo_frame);
            img.setImageResource(frameId);

            img = (ImageView) dialogView.findViewById(R.id.editMenWomen);
            if (openInDialog.leafId == 1) {
                img.setVisibility(android.view.View.VISIBLE);
                img.setImageResource(openInDialog.isWomen() ? R.drawable.woomen
                        : R.drawable.men);
            }

            img = (ImageView) dialogView.findViewById(R.id.editGoOut);
            img.setImageResource(openInDialog.isGoOut() ? R.drawable.goout
                    : R.drawable.goout_not);

            TextView img_text = (TextView) dialogView
                    .findViewById(R.id.big_photo_text);
            img_text.setText(openInDialog.getName());
            img_text = (TextView) dialogView.findViewById(R.id.birthday);
            img_text.setText(dateFormat(openInDialog.getBirthday()));
            ImageView image = (ImageView) dialogView
                    .findViewById(R.id.close_dialog);
            //set actions when dialog close
            image.setOnClickListener(new OnClickListener() {
                // @Override
                public void onClick(View v) {
                    EditText t = (EditText) dialogView.findViewById(R.id.editTextInDialog);
                    if (t.getVisibility() == android.view.View.VISIBLE) {
                        openInDialog.setName(t.getText().toString());
                        FamilyTree.dbWorker.saveRelative(openInDialog);
                        int id = getResources().getIdentifier(
                                FamilyTree.PACKAGE_NAME + ":id/leaf"
                                        + openInDialog.getLeafId() + "Text", null, null);
                        leafText.setText(openInDialog.name);
                    }
                    dialog.dismiss();
                }
            });
            image = (ImageView) dialogView
                    .findViewById(R.id.editInDialog);
            //set actions when text edit
            image.setOnClickListener(new OnClickListener() {
                // @Override
                public void onClick(View v) {
                    final EditText t = (EditText) dialogView
                            .findViewById(R.id.editTextInDialog);
                    TextView tv = (TextView) dialogView.findViewById(R.id.big_photo_text);

                    if (t.getVisibility() == android.view.View.VISIBLE) {
                        //action when save edit
                        ((ImageView) dialogView
                                .findViewById(R.id.editInDialog)).setImageResource(R.drawable.edit);
                        t.setVisibility(android.view.View.INVISIBLE);

                        openInDialog.setName(t.getText().toString());
                        FamilyTree.dbWorker.saveRelative(openInDialog);
                        tv.setVisibility(android.view.View.VISIBLE);
                        tv.setText(openInDialog.name);
                        leafText.setText(openInDialog.name);

                        // close keyboard
                        t.requestFocus();
                        t.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                keyboard.hideSoftInputFromWindow(t.getWindowToken(), 0);
                                // TODO Auto-generated method stub
                            }
                        }, 50);
                    } else {
                        //action when start edit
                        ((ImageView) dialogView
                                .findViewById(R.id.editInDialog)).setImageResource(R.drawable.save);
                        t.setVisibility(android.view.View.VISIBLE);
                        tv.setVisibility(android.view.View.INVISIBLE);
                        t.setText(openInDialog.name);
                        // open keyboard
                        t.requestFocus();
                        t.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                keyboard.showSoftInput(t, 0);
                                // TODO Auto-generated method stub
                            }
                        }, 50);

                    }
                }
            });
            TextView tv = (TextView) dialogView
                    .findViewById(R.id.big_photo_text);
            tv.setOnClickListener(new OnClickListener() {
                // @Override
                public void onClick(View v) {
                    final EditText t = (EditText) dialogView
                            .findViewById(R.id.editTextInDialog);
                    TextView tv = (TextView) dialogView.findViewById(R.id.big_photo_text);

                    if (t.getVisibility() == android.view.View.VISIBLE) {
                        //action when save edit
                        ((ImageView) dialogView
                                .findViewById(R.id.editInDialog)).setImageResource(R.drawable.edit);
                        t.setVisibility(android.view.View.INVISIBLE);

                        openInDialog.setName(t.getText().toString());
                        FamilyTree.dbWorker.saveRelative(openInDialog);
                        tv.setVisibility(android.view.View.VISIBLE);
                        tv.setText(openInDialog.name);
                        leafText.setText(openInDialog.name);

                        // close keyboard
                        t.requestFocus();
                        t.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                keyboard.hideSoftInputFromWindow(t.getWindowToken(), 0);
                                // TODO Auto-generated method stub
                            }
                        }, 50);
                    } else {
                        //action when start edit
                        ((ImageView) dialogView
                                .findViewById(R.id.editInDialog)).setImageResource(R.drawable.save);
                        t.setVisibility(android.view.View.VISIBLE);
                        tv.setVisibility(android.view.View.INVISIBLE);
                        t.setText(openInDialog.name);
                        // open keyboard
                        t.requestFocus();
                        t.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                keyboard.showSoftInput(t, 0);
                                // TODO Auto-generated method stub
                            }
                        }, 50);

                    }
                }
            });
            image = (ImageView) dialogView
                    .findViewById(R.id.editGoOut);
            //set actions go out change
            image.setOnClickListener(new OnClickListener() {
                // @Override
                public void onClick(View v) {
                    openInDialog.setGoOut(!openInDialog.isGoOut());
                    FamilyTree.dbWorker.saveRelative(openInDialog);
                    Log.d(LOG_TAG, "openInDialog.isGoOut() = " + openInDialog.isGoOut());
                    ImageView img = (ImageView) dialogView
                            .findViewById(R.id.big_photo_frame);
                    int frameId = FamilyTree.selectFrame(openInDialog);
                    img.setImageResource(frameId);
                    img = (ImageView) dialogView.findViewById(R.id.editGoOut);
                    img.setImageResource(openInDialog.isGoOut() ? R.drawable.goout
                            : R.drawable.goout_not);
                    leafFrame.setImageResource(frameId);
                }
            });
            image = (ImageView) dialogView
                    .findViewById(R.id.editMenWomen);
            //set actions when change gender
            image.setOnClickListener(new OnClickListener() {
                // @Override
                public void onClick(View v) {
                    openInDialog.setWomen(!openInDialog.isWomen());
                    FamilyTree.dbWorker.saveRelative(openInDialog);
                    ImageView img = (ImageView) dialogView
                            .findViewById(R.id.big_photo_frame);
                    int frameId = FamilyTree.selectFrame(openInDialog);
                    img.setImageResource(frameId);
                    leafFrame.setImageResource(frameId);
                    img = (ImageView) dialogView.findViewById(R.id.editMenWomen);
                    img.setImageResource(openInDialog.isWomen() ? R.drawable.woomen
                            : R.drawable.men);
                }
            });
            image = (ImageView) dialogView

                    .findViewById(R.id.editBirthday);
            //open birthday dialog
            image.setOnClickListener(new OnClickListener() {
                // @Override
                public void onClick(View v) {
                    DialogFragment newFragment = new DatePickerFragment(openInDialog, dialogView, leafBirthday);
                    newFragment.show(fragmentManager, "datePicker");
                    // Context.showDialog(DIALOG_DATE);
                }
            });
            tv = (TextView) dialogView
                    .findViewById(R.id.birthday);
            tv.setOnClickListener(new OnClickListener() {
                // @Override
                public void onClick(View v) {
                    DialogFragment newFragment = new DatePickerFragment(openInDialog, dialogView, leafBirthday);
                    newFragment.show(fragmentManager, "datePicker");
                    // Context.showDialog(DIALOG_DATE);
                }
            });


            image = (ImageView) dialogView
                    .findViewById(R.id.big_photo);

            //open pick photo dialog
            image.setOnClickListener(new OnClickListener() {
                // @Override
                public void onClick(View v) {
                    FragmentManager fm = fragmentManager;
                    OpenPhoto fragment = new OpenPhoto(openInDialog, dialogView, leaf, context);
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.add(android.R.id.content, fragment, "myFragmentTag");
                    ft.commit();
                }
            });

            image = (ImageView) dialogView
                    .findViewById(R.id.makePhoto);
            //make photo dialog
            image.setOnClickListener(new OnClickListener() {
                // @Override
                public void onClick(View v) {
                    FragmentManager fm = fragmentManager;
                    MakePhoto fragment = new MakePhoto(openInDialog, dialogView, leaf, context);
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.add(android.R.id.content, fragment, "makePhotoFragment");
                    ft.commit();
                }
            });


            dialog.setView(dialogView);
            dialog.show();

        }

    };

    private void editInDialog(View v) {
        final EditText t = (EditText) dialogView
                .findViewById(R.id.editTextInDialog);
        TextView tv = (TextView) dialogView.findViewById(R.id.big_photo_text);

        if (t.getVisibility() == android.view.View.VISIBLE) {
            //action when save edit
            ((ImageView) dialogView
                    .findViewById(R.id.editInDialog)).setImageResource(R.drawable.edit);
            t.setVisibility(android.view.View.INVISIBLE);

            openInDialog.setName(t.getText().toString());
            FamilyTree.dbWorker.saveRelative(openInDialog);
            tv.setVisibility(android.view.View.VISIBLE);
            tv.setText(openInDialog.name);
            leafText.setText(openInDialog.name);

            // close keyboard
            t.requestFocus();
            t.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.hideSoftInputFromWindow(t.getWindowToken(), 0);
                    // TODO Auto-generated method stub
                }
            }, 50);
        } else {
            //action when start edit
            ((ImageView) dialogView
                    .findViewById(R.id.editInDialog)).setImageResource(R.drawable.save);
            t.setVisibility(android.view.View.VISIBLE);
            tv.setVisibility(android.view.View.INVISIBLE);
            t.setText(openInDialog.name);
            // open keyboard
            t.requestFocus();
            t.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(t, 0);
                    // TODO Auto-generated method stub
                }
            }, 50);

        }
    }

    private Bitmap readPhoto(DbWorker.Relative r) throws Exception {
        Bitmap bm = null;
        try {
            bm = decodeFile(extStorageDirectory + "/familytree" + r.getLeafId()
                    + ".png");
        } catch (Exception e) {
            Log.d(LOG_TAG, "error read leaf " + e.toString());
            bm = decodeFile(r.photoUrl);
        }
        return bm;
    }

    public static String dateFormat(Date d) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(d == null ? new Date() : d);
    }

    public static String dateFormatShort(Date d) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        return dateFormat.format(d);
    }


    private Bitmap decodeFile(String f) throws Exception {
        Bitmap b = null;

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        Log.d(LOG_TAG, "try save =" + f);

        FileInputStream fis = new FileInputStream(f);
        BitmapFactory.decodeStream(fis, null, o);
        fis.close();

        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(
                    2,
                    (int) Math.round(Math.log(IMAGE_MAX_SIZE
                            / (double) Math.max(o.outHeight, o.outWidth))
                            / Math.log(0.5)));
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        fis = new FileInputStream(f);
        b = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();

        return b;
    }

    public void setLeafText(String text) {
        this.leafText.setText(text);
    }
}