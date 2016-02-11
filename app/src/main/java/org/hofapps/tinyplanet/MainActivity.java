package org.hofapps.tinyplanet;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.app.Fragment;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements PlanetMaker.PlanetChangeCallBack, MediaScannerConnectionClient, SettingsFragment.FragmentVisibilityCallBack, ActivityCompat.OnRequestPermissionsResultCallback {
//public class MainActivity extends ActionBarActivity implements PlanetMaker.PlanetChangeCallBack {

    private NativeWrapper nativeWrapper;
    private Mat originalImg, transformedImg;
    private ImageView imageView;
    private PlanetMaker previewPlanetMaker;
    private CoordinatorLayout coordinatorLayout;
    private int[] sizeMinMax;

    private GesturesDialogFragment gestureFragment;
    private SamplesFragment samplesFragment;

    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE= 2;

    private static final int PICK_IMAGE_REQUEST = 1;

    public static final int MAX_IMG_SIZE = 3000;

    private static final int MENU_ITEM_GALLERY = 0;
    private static final int MENU_ITEM_SHARE = 1;

    private int menuItem = -1;

    private static final String TAG = "Touch";
    private MainActivityFragment mainActivityFragment;
    private OnPlanetTouchListener onPlanetTouchListener;
    private MediaScannerConnection mediaScannerConnection;
    private LinearLayout settingsTitle;

    private Context context;

    static {
        System.loadLibrary("wrapper");
        System.loadLibrary("opencv_java");
    }


//    public void onRadioButtonClicked(View view) {
//        // Is the button now checked?
//        boolean checked = ((RadioButton) view).isChecked();
//
//        // Check which radio button was clicked
//        switch(view.getId()) {
//            case R.id.viewModeRadioButton:
//                if (checked)
//                    onPlanetTouchListener.setGestureMode(false);
//                    imageView.setScaleType(ImageView.ScaleType.MATRIX);
//                    break;
//            case R.id.editModeRadioButton:
//                if (checked) {
//                    onPlanetTouchListener.setGestureMode(true);
//                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                    break;
//                }
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        onPlanetTouchListener = new OnPlanetTouchListener(this);

        imageView.setOnTouchListener(onPlanetTouchListener);



//        imageContainerView = (LinearLayout) findViewById(R.id.imageContainerView);

        // TODO: Put this into MainActivityFragment and connect via callbacks

//        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.modeRadioGroup);
//        radioGroup.check(R.id.editModeRadioButton);

        nativeWrapper = new NativeWrapper();

        sizeMinMax = getResources().getIntArray(R.array.size_seekbar_values);

        previewPlanetMaker = new PlanetMaker(nativeWrapper, 500, sizeMinMax);

        // TODO: Check why we need here getChildFragmentManager instead of getFragmentManager and set sdkMinVersion to 15 back!


        FragmentManager fragmentManager = getFragmentManager();
        mainActivityFragment = (MainActivityFragment) fragmentManager.findFragmentById(R.id.main_fragment);

        mainActivityFragment.initSeekBarValues((int) previewPlanetMaker.getSize(), (int) previewPlanetMaker.getScale(), (int) previewPlanetMaker.getAngle());


        Intent intent = getIntent();

        // Did the user intended to open an image?
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {

            Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                FileLoader fileLoader = new FileLoader(this);
                fileLoader.execute(imageUri);
            }


        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_open_file) {

            Intent intent = new Intent();
            // Show only images, no videos or anything else
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

            return true;
        } else if (id == R.id.action_save_file) {

//            saveFile();
            requestFileSave();

        } else if (id == R.id.action_share) {

            openGallery();

        }

        else if (id == R.id.action_about) {

            showAboutDialog();

        }

        else if (id == R.id.action_reset) {

            resetPlanetValues();

        }

        else if (id == R.id.action_samples) {

            showSamplesFragment();

        }

        else if (id == R.id.action_help) {

            showGestureFragment();

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            FileLoader fileLoader = new FileLoader(this);
            fileLoader.execute(uri);

        }
    }

    private void updateImageView() {

        if (!previewPlanetMaker.getIsImageLoaded())
            return;

        Mat previewImg = previewPlanetMaker.getPlanetImage();

        Bitmap bm = Bitmap.createBitmap(previewImg.cols(), previewImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(previewImg, bm);

        imageView.setImageBitmap(bm);

        previewPlanetMaker.releasePlanetImage();

        imageView.setScaleType(ImageView.ScaleType.MATRIX);

    }

    @Override
    public void onSizeChange(int size) {

        previewPlanetMaker.setSize((double) size);
        updateImageView();

    }

    @Override
    public void onScaleChange(int scale) {

        previewPlanetMaker.setScale((double) scale);
        updateImageView();

    }

    @Override
    public void onAngleChange(int angle) {

        previewPlanetMaker.setAngle((double) angle);
        updateImageView();

    }

    @Override
    public void addAngle(float angle) {

//        if (angle > angleMinMax[SettingsFragment.ARRAY_MAX_POS])
//            angle = angleMinMax[SettingsFragment.ARRAY_MIN_POS];
//        else if (angle < angleMinMax[SettingsFragment.ARRAY_MIN_POS])
//            angle = angleMinMax[SettingsFragment.ARRAY_MAX_POS];

        previewPlanetMaker.addAngle(angle);
        updateImageView();

        mainActivityFragment.setAngleBarValue((int) previewPlanetMaker.getAngle());

    }

    @Override
    public void addScaleLog(float scale) {

        previewPlanetMaker.addScale(scale);
        updateImageView();

        mainActivityFragment.setSizeBarValue((int) previewPlanetMaker.getSize());

    }

    @Override
    public void onInvertChange(boolean isInverted) {

        previewPlanetMaker.invert(isInverted);
        updateImageView();

    }

    @Override
    public void onVisibilityChange() {

        float y = settingsTitle.getY();

        LayoutParams params = imageView.getLayoutParams();
        params.height = (int) y;


//        int h = imageContainerView.getHeight();
//        int w = imageContainerView.getWidth();

    }

    public static interface PlanetInitCallBack {

        void onInit(int size, int scale, int angle);

    }

    private void resetPlanetValues() {

        previewPlanetMaker.reset();

        mainActivityFragment.setAngleBarValue((int) previewPlanetMaker.getAngle());
        mainActivityFragment.setSizeBarValue((int) previewPlanetMaker.getSize());
        mainActivityFragment.setZoomBarValue((int) previewPlanetMaker.getScale());
        mainActivityFragment.setInvertPlanetSwitch(previewPlanetMaker.getIsPlanetInverted());

        updateImageView();

    }

    private void openGallery() {

//        File mediaStorageDir = getMediaStorageDir();
////
//        Uri uri = Uri.fromFile(lastFile);
////
////        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
////        startActivity(intent);
//
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_VIEW);
//        intent.setDataAndType(uri, "image/*");
//        startActivity(intent);

        startScan(MENU_ITEM_GALLERY);

    }

    @Override
    public void onMediaScannerConnected() {

        File mediaStorageDir = getMediaStorageDir();

        String[] files = mediaStorageDir.list();

        if (files == null) {

            showNoFileFoundDialog();
            return;

        }

        //	    Opens the most recent image:
        Arrays.sort(files);

        String fileName = mediaStorageDir.toString() + "/" + files[files.length - 1];


        mediaScannerConnection.scanFile(fileName, null);
    }

    private void showGestureFragment() {

        if (gestureFragment == null)
            gestureFragment = new GesturesDialogFragment();

        gestureFragment.show(getFragmentManager(), "gesture_fragment");

    }

    private void showSamplesFragment() {

        if (samplesFragment == null)
            samplesFragment = new SamplesFragment();

        samplesFragment.show(getFragmentManager(), "samples_fragment");

    }

    private void showFragment(Fragment fragment) {

        FragmentManager fragmentManager = getFragmentManager();
        android.app.FragmentTransaction ft = fragmentManager.beginTransaction();
//        ft.add(R.id.container, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void showNoFileFoundDialog() {

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.no_file_found_msg).setTitle(R.string.no_file_found_title);

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();

    }

    private void startScan(int menuItem) {

        this.menuItem = menuItem;

        if(mediaScannerConnection != null)
            mediaScannerConnection.disconnect();


        mediaScannerConnection = new MediaScannerConnection(this, this);
        mediaScannerConnection.connect();

    }

    @Override
    public void onScanCompleted(String path, Uri uri) {

        try {


            if (uri != null) {
                if (menuItem == MENU_ITEM_GALLERY) {

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    startActivity(intent);

                }
                else if (menuItem == MENU_ITEM_SHARE) {
                    //
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("*/*");

                    //        	    TODO: use R.string.text instead of hard-coded string. Do not know why an exception is thrown here:
                    //        	    String shareText = (String) getString(R.string.share_text);
                    //        	    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out Zelfie - Zombie Camera: https://play.google.com/store/apps/details?id=com.zelfie.zelfiecam");

                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(shareIntent);
                }


            }
        }
        finally {
            mediaScannerConnection.disconnect();
            mediaScannerConnection = null;
        }

    }


    // This method is used to enable file saving in marshmallow (Android 6), since in this version file saving is not allowed without user permission:
    private void requestFileSave() {

        // Check Permissions Now
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // ask for permission:
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
        else
            saveFile();

    }


    private void saveFile() {


//        // marshmallow try:
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            // Check Permissions Now
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
//        }
////
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//
//            showNoSavingPermissionDialog();
//            return;
//
//        }
        // try end



        Uri uri = getOutputMediaFile();

        if (uri != null) {

            FileSaver fileSaver = new FileSaver(this);
            fileSaver.execute(uri);
//            Mat planet = previewPlanetMaker.getFullResPlanet();
//
//            if (planet == null) {
//                return;
//            }
//
//
//            Imgproc.cvtColor(planet, planet, Imgproc.COLOR_BGR2RGB);
//
//            boolean imgSaved = Highgui.imwrite(outFile.toString(), planet);
//
//            planet.release();
//
//            if (imgSaved) {
//                // Tell the media scanner about the new file so that it is
//                // immediately available to the user.
//                MediaScannerConnection.scanFile(this,
//                        new String[]{outFile.toString()}, null,
//                        new MediaScannerConnection.OnScanCompletedListener() {
//                            public void onScanCompleted(String path, Uri uri) {
//
//                            }
//                        });
//
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast toast = Toast.makeText(this, R.string.file_saved_msg, duration);
//                toast.show();
//
//            }




        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveFile();
            } else {
                showNoSavingPermissionDialog();
            }
        }
    }


    private Uri getOutputMediaFile() {

        // Check if we can access the external storage:
        if (!isExternalStorageWritable()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(R.string.external_storage_not_accessible_msg)
                    .setTitle(R.string.external_storage_not_accessible_title);

            AlertDialog dialog = builder.create();

            dialog.show();

            return null;


        }



        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        File mediaStorageDir = getMediaStorageDir();
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
//	            Log.d("TinyPlanet", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        Uri uri = Uri.fromFile(mediaFile);

        return uri;

    }

    private File getMediaStorageDir() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "TinyPlanet");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
//	            Log.d("Zelfie", "failed to create directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;

    }

    // cannot save image dialog:

    private void showNoSavingPermissionDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.no_saving_permission_msg).setTitle(R.string.no_saving_permission_title);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    // About dialog:

    private void showAboutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.about_msg).setTitle(R.string.action_help_about_title);
        builder.setIcon(R.drawable.tiny_planet_gray_300px);
        AlertDialog dialog = builder.create();
//        dialog.setFeatureDrawable(Window.FEATURE_LEFT_ICON, R.drawable.tiny_planet_gray_300px);
        dialog.setIcon(R.drawable.tiny_planet_gray_300px);
        dialog.show();

    }

    // ==================================== File Handler Classes ====================================

    private abstract class FileHandler extends AsyncTask<Uri, Void, Void> {


        protected Context context;
        protected String spinnerText;
        private ProgressDialog progressDialog;

        public FileHandler() {

        }

        public FileHandler(Context context)
        {
            this.context = context;
        }

//        protected abstract void performTask();

        protected void onPreExecute() {

            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(spinnerText);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        protected void onPostExecute(Void dummy) {
            // The Void dummy argument is necessary so that onPostExecute gets called.
            progressDialog.dismiss();
        }

    }

    private class FileLoader extends FileHandler {


        public FileLoader(Context context) {

            super(context);
            // TODO: check why this is necessary:
            this.context = context;
            spinnerText = getResources().getString(R.string.file_load_text);

        }

        protected Void doInBackground(Uri... uris) {

            try {

                AssetFileDescriptor fileDescriptor;
                fileDescriptor = getContentResolver().openAssetFileDescriptor(uris[0], "r");

                Bitmap bitmap = ImageReader.decodeSampledBitmapFromResource(getResources(), fileDescriptor, MAX_IMG_SIZE, MAX_IMG_SIZE);
                previewPlanetMaker.setInputImage(bitmap);

                // The image view cannot be touched inside the thread because it has been created on the UI thread.
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // The image view is initialized with a fixed height in order to show the 'gray planet' in a nice manner. Now we need to undo this initialization:
//                        imageView.getLayoutParams().height = LayoutParams.MATCH_PARENT;
                        updateImageView();

                        SharedPreferences pref = getSharedPreferences("org.hofapps.tinyplanet", Context.MODE_PRIVATE);

                        if (pref.getBoolean("firstRun", true))
                            showAboutDialog();

                        pref.edit().putBoolean("firstRun", false).commit();


                    }

                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();

            }

            return null;

        }



    }

    private class FileSaver extends FileHandler {

        public FileSaver(Context context) {

            super(context);
            // TODO: check why this is necessary:
            this.context = context;

            spinnerText = getResources().getString(R.string.file_save_text);

        }

        @Override
        protected Void doInBackground(Uri... uris) {

            if (previewPlanetMaker == null)
                return null;

            Mat planet = previewPlanetMaker.getFullResPlanet();

            if (planet == null) {
                return null;
            }

            Imgproc.cvtColor(planet, planet, Imgproc.COLOR_BGR2RGB);

            final File outFile = new File(uris[0].getPath());

            boolean imgSaved = Highgui.imwrite(outFile.toString(), planet);

            planet.release();

            if (imgSaved) {

                 MediaScannerConnection.scanFile(context,
                        new String[]{outFile.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {

                            public void onScanCompleted(String path, Uri uri) {


                            }
                });
            }



            return null;


        }


    }


}
