package com.flywolf.familytree;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.flywolf.familytree.DbWorker.Relative;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

public class FamilyTree extends Activity {
    LinearLayout L1;
    ImageView image;
    public static DbWorker dbWorker;
    private int pickImage = 1;
    final String LOG_TAG = "myLogs";
    public static String PACKAGE_NAME;
    static int IMAGE_MAX_SIZE = 300;
    private boolean editMode = false;
    int DIALOG_DATE = 1;
    private boolean showBirthday = true;
    private boolean showName = true;
    SharedPreferences sPref;
    final String PREFNAME = "FamilyTree";
    public static final String extStorageDirectory = Environment
            .getExternalStorageDirectory().toString() + "/familytree";
    private LinearLayout framesContainer;
    public static FamilyTree familyTree;
    private DbWorker.Relative openInDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        familyTree = this;
        setContentView(R.layout.activity_family_tree);
        dbWorker = new DbWorker(this);
        dbWorker.readFromDb();
        PACKAGE_NAME = getApplicationContext().getPackageName();
        L1 = (LinearLayout) findViewById(R.id.family_tree);
        ImageButton but = (ImageButton) findViewById(R.id.create_screen);
        but.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout tt = (LinearLayout) findViewById(R.id.top_tree);
                tt.setVisibility(android.view.View.INVISIBLE);
                View v1 = L1.getRootView();
                v1.setDrawingCacheEnabled(true);
                Bitmap bm = v1.getDrawingCache();
                toJPEGFile(bm);
                tt.setVisibility(android.view.View.VISIBLE);
            }
        });
        Iterator<Relative> it = dbWorker.relative.iterator();
        int leafsOnLevel = 1;
        int generationId = 2;
        int leafsInGeneration = 1;
        int id = getResources().getIdentifier(
                FamilyTree.PACKAGE_NAME + ":id/frames_container1",
                null, null);
        framesContainer = (LinearLayout) findViewById(id);
        while (it.hasNext()) {
            Relative cs = it.next();
            Log.d(LOG_TAG, "path leaf = " + ":id/leaf" + cs.getLeafId());
            id = getResources().getIdentifier(
                    FamilyTree.PACKAGE_NAME + ":id/leaf" + cs.getLeafId(),
                    null, null);
            int textId = getResources().getIdentifier(
                    FamilyTree.PACKAGE_NAME + ":id/leaf" + cs.getLeafId()
                            + "Text", null, null);

            LeafFrame frame = new LeafFrame(getApplicationContext());
            frame.setLeafText(cs.name);
            if (cs.getBirthday() != null) {
                frame.setLeafBirthday(dateFormatShort(cs.getBirthday()));
                frame.setLeafBirthdayVisibility(true);
            } else {
                frame.setLeafBirthdayVisibility(false);
            }
            Log.d(LOG_TAG, "read photo " + cs.getImgB());
            if (cs.getImgB() != null) {
                IMAGE_MAX_SIZE = 150;

                    frame.setLeaf(DbWorker.getImage(cs.getImgB()));
              } else {
                if (cs.id != 1)
                    frame.setLeaf((cs.id % 2 == 0) ? R.drawable.leaf2m : R.drawable.leaf2w);
            }
            frame.setLeafFrame(selectFrame(cs));
            frame.setOpenInDialog(cs);
            frame.setContext(FamilyTree.this);
            frame.setFragmentManager(getFragmentManager());
            // frame.setLeafFrame(id);
            framesContainer.addView(frame);
            Log.d(LOG_TAG, "leafsOnLevel" + leafsOnLevel + "=" + leafsInGeneration + "generationId" + generationId);
            if (leafsOnLevel == leafsInGeneration) {
                leafsInGeneration = 0;
                leafsOnLevel = leafsOnLevel * 2;
                Log.d(LOG_TAG, "id/frames_container" + leafsOnLevel);
                id = getResources().getIdentifier(
                        FamilyTree.PACKAGE_NAME + ":id/frames_container" + generationId++,
                        null, null);
                framesContainer = (LinearLayout) findViewById(id);
            }
            leafsInGeneration++;
        }
        //set bg from pref
        sPref = getSharedPreferences(PREFNAME, MODE_PRIVATE);
        int bgId = sPref.getInt("bg_id", 0);
        showBirthday = sPref.getBoolean("show_birthday", true);
        showName = sPref.getBoolean("show_name", true);
        LinearLayout bgElement = (LinearLayout) findViewById(R.id.activity_family_tree);
        id = getResources().getIdentifier(
                FamilyTree.PACKAGE_NAME + ":drawable/bg"
                        + (bgId == 0 ? "" : bgId), null, null);
        if (Build.VERSION.SDK_INT > 15) {
            Drawable d = getResources().getDrawable(id);
            bgElement.setBackground(d);
        }

    }

    public void openGenerationTitle(View view) {
        int id = getResources().getIdentifier(
                getPackageName() + ":string/" + (String) view.getTag()
                , null, null);
        Toast.makeText(view.getContext(), getString(id), Toast.LENGTH_LONG)
                .show();
    }

     int bgId;

    @SuppressLint("NewApi")
    public void changeBg(View view) {
        if (bgId > 8)
            bgId = 0;
        else
            bgId++;
        LinearLayout bgElement = (LinearLayout) findViewById(R.id.activity_family_tree);
        int id = getResources().getIdentifier(
                FamilyTree.PACKAGE_NAME + ":drawable/bg"
                        + (bgId == 0 ? "" : bgId), null, null);
        Drawable d = getResources().getDrawable(id);
        bgElement.setBackground(d);
        sPref = getSharedPreferences(PREFNAME, MODE_PRIVATE);
        int totalOpenCount = sPref.getInt("total_open_count", 0);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt("bg_id", bgId);
        ed.commit();

    }

    public void showBirthday(View view) {
        showBirthday = !showBirthday;
        for (int i = 1; i <= 7; i++) {
            int id = getResources().getIdentifier(
                    FamilyTree.PACKAGE_NAME + ":id/frames_container" + i,
                    null, null);
            LinearLayout layout = (LinearLayout) findViewById(id);
            for (int j = 0; j < layout.getChildCount(); j++) {
                View v = layout.getChildAt(j);
                if (v instanceof LeafFrame) {
                    ((LeafFrame) v).setLeafBirthdayVisibility(showBirthday);
                }
            }
        }
        sPref = getSharedPreferences(PREFNAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean("show_birthday", showBirthday);
        ed.commit();
    }

    public void showName(View view) {
        showName = !showName;
        for (int i = 1; i <= 7; i++) {
            int id = getResources().getIdentifier(
                    FamilyTree.PACKAGE_NAME + ":id/frames_container" + i,
                    null, null);
            LinearLayout layout = (LinearLayout) findViewById(id);
            for (int j = 0; j < layout.getChildCount(); j++) {
                View v = layout.getChildAt(j);
                if (v instanceof LeafFrame) {
                    ((LeafFrame) v).setLeafTextVisibility(showName);
                }
            }
        }
        sPref = getSharedPreferences(PREFNAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean("show_name", showName);
        ed.commit();
    }
/*
    protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_DATE) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(openInDialog.getBirthday() == null ? new Date()
					: openInDialog.getBirthday());
			DatePickerDialog tpd = new DatePickerDialog(this, myCallBack,
					cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));
			try {
				// Hack to (try to) force minDate to 1888 instead of 1970.
				DatePicker dp = tpd.getDatePicker();
				Calendar minCal = Calendar.getInstance();
				minCal.set(1000, Calendar.JANUARY, 1, 0, 0, 0);
				dp.setMinDate(minCal.getTimeInMillis());

			} catch (NoSuchMethodError e) {
				Log.d(LOG_TAG, e.toString());
				e.printStackTrace();
			}

			return tpd;
		}

		return super.onCreateDialog(id);
	}

	OnDateSetListener myCallBack = new OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			GregorianCalendar gk = new GregorianCalendar(year, monthOfYear,
					dayOfMonth);
			// Log.d(LOG_TAG, "goToDate = " + gk.getTime());
			gk.add(Calendar.DATE, 1);
			openInDialog.setBirthday(gk.getTime());
			dbWorker.saveRelative(openInDialog);
			TextView text = (TextView) LeafFrame.dialogView
					.findViewById(R.id.big_photo_text);
			text.setText(openInDialog.getName());
			text = (TextView) LeafFrame.dialogView.findViewById(R.id.birthday);
			text.setText(dateFormat(openInDialog.getBirthday()));
			
			int id = getResources()
					.getIdentifier(
							FamilyTree.PACKAGE_NAME + ":id/leaf"
									+ openInDialog.getLeafId() + "Birthday",
							null, null);
			text = (TextView) findViewById(id);
			text.setVisibility(android.view.View.VISIBLE);
			text.setText(dateFormatShort(openInDialog.getBirthday()));

			// goToDate(gk.getTime());
			// pagerTop.setCurrentItem(moonData.getId());

		}
	};
*/

    public static String dateFormat(Date d) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(d == null ? new Date() : d);
    }

    public static String dateFormatShort(Date d) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        return dateFormat.format(d);
    }

    public static int selectFrame(Relative r) {
        int frameId;
        if (r.isGoOut()) {
            if (r.leafId == 1)
                frameId = (r.women) ? R.drawable.framewgoout
                        : R.drawable.framemgoout;
            else
                frameId = (r.leafId % 2 != 0) ? R.drawable.framewgoout
                        : R.drawable.framemgoout;
        } else {
            if (r.leafId == 1)
                frameId = (r.women) ? R.drawable.framew : R.drawable.framem;
            else
                frameId = (r.leafId % 2 != 0) ? R.drawable.framew
                        : R.drawable.framem;
        }
        return frameId;

    }


    public void sendEmail(View view) {
        // writeCsv("details.csv");
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/html");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.email_subject));
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html
                .fromHtml(new StringBuilder().append(
                        String.format(getString(R.string.email_body),
                                "<a href='" + getString(R.string.market_url)
                                        + "'>" + getString(R.string.app_name)
                                        + "</a>")).toString()));

        Uri uri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(), "details.csv"));
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(emailIntent, "Email:"));
    }

	/*public static Bitmap getclip(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
				bitmap.getWidth() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}*/

    /*	public Bitmap toGrayscale(Bitmap bmpOriginal) {
            int width, height;
            height = bmpOriginal.getHeight();
            width = bmpOriginal.getWidth();

            Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
                    Bitmap.Config.RGB_565);
            Canvas c = new Canvas(bmpGrayscale);
            Paint paint = new Paint();
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
            paint.setColorFilter(f);
            c.drawBitmap(bmpOriginal, 0, 0, paint);
            return bmpGrayscale;
        }
    */
	/*public static Bitmap createSepiaToningEffect(Bitmap src) {
		return createSepiaToningEffect(src, 50, 1.5, 0.6, 0.12);
		// 0.3, 0.5, 0.59
		// 1.5, 0.6, 0.12
	}

	public static Bitmap createSepiaToningEffect(Bitmap src, int depth,
			double red, double green, double blue) {
		// image size
		int width = src.getWidth();
		int height = src.getHeight();
		// create output bitmap
		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
		// constant grayscale
		final double GS_RED = 0.3;
		final double GS_GREEN = 0.59;
		final double GS_BLUE = 0.11;
		// color information
		int A, R, G, B;
		int pixel;

		// scan through all pixels
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				// get pixel color
				pixel = src.getPixel(x, y);
				// get color on each channel
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);
				// apply grayscale sample
				B = G = R = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);

				// apply intensity level for sepid-toning on each channel
				R += (depth * red);
				if (R > 255) {
					R = 255;
				}

				G += (depth * green);
				if (G > 255) {
					G = 255;
				}

				B += (depth * blue);
				if (B > 255) {
					B = 255;
				}

				// set new pixel color to output image
				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}

		// return final image
		return bmOut;
	}
*/
    private void toJPEGFile(Bitmap inBitmap) {
        // TODO Auto-generated method stub
        File myDir = new File(extStorageDirectory);
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "familytreecapture" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            inBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out); // ERROR 341
            // LINE
            out.flush();
            out.close();
            Toast toast = Toast.makeText(getApplicationContext(),
                    getString(R.string.screen_saved) + myDir + "/" + fname,
                    Toast.LENGTH_SHORT);
            toast.show();
            galleryAddPic(file);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void galleryAddPic(File file) {
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.family_tree, menu);
        return true;
    }

	/*public void imageFilter(View v) {
		ImageView iv = (ImageView) findViewById(R.id.leaf1);
		iv.setColorFilter(Color.RED, PorterDuff.Mode.SRC_OVER);
	}*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

	/*@SuppressLint("NewApi")
	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			CursorLoader loader = new CursorLoader(this, contentUri, proj,
					null, null, null);
			cursor = loader.loadInBackground();
		} else {
			cursor = managedQuery(contentUri, proj, null, null, null);
		}
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
*/
/*	public Bitmap readBitmap(Uri selectedImage) {
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2; // reduce quality
		AssetFileDescriptor fileDescriptor = null;
		try {
			fileDescriptor = this.getContentResolver().openAssetFileDescriptor(
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
*/

/*
    public static String savePhoto(Bitmap bm, DbWorker.Relative openInDialog) {
        String photoUrl="";
        try {
            //  Log.d(LOG_TAG, "try save " + FamilyTree.extStorageDirectory + "familytree");
            File wallpaperDirectory = new File(FamilyTree.extStorageDirectory);
// have the object build the directory structure, if needed.
            wallpaperDirectory.mkdirs();
// create a File object for the output file
            // File outputFile = new File(wallpaperDirectory, filename);
            photoUrl=FamilyTree.extStorageDirectory+"familytree"
                    + openInDialog.getLeafId() + ".png";
            File file = new File(wallpaperDirectory, "familytree"
                    + openInDialog.getLeafId() + ".png");
            OutputStream outStream;
            outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, outStream);
            outStream.flush();
            outStream.close();
            // Log.d(LOG_TAG, "photo saved " + FamilyTree.extStorageDirectory + "familytree"
            //        + openInDialog.getLeafId() + ".png");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "file:/"+photoUrl;
    }
*/
}
