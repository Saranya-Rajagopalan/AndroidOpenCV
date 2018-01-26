package com.example.stars.androidopencv;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private static final int REQUEST_CODE = 100;
    private static final Scalar RED = new Scalar ( 255, 0, 0 );
    private static final Scalar GREEN = new Scalar ( 0, 255, 0 );
    private static final Scalar BLUE = new Scalar ( 0, 0, 255 );
    private static final Scalar YELLOW = new Scalar ( 255, 255, 0, 255 );
    private static final Scalar MAGENTA = new Scalar ( 255, 0, 255, 255 );
    private static final Scalar CYAN = new Scalar ( 0, 255, 255, 255 );
    private static final Scalar WHITE = new Scalar ( 255, 255, 255, 255 );
    private static Scalar highlight_mask = WHITE;
    int color = -1;
    private Button save_button;
    private Switch highlight_button, show_object;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba, mHSV;
    private boolean highlighted, show_object_status;

    private boolean save = false;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback ( this ) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    mOpenCvCameraView.enableView ();
                    break;
                default: {
                    super.onManagerConnected ( status );
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );

        getWindow ().addFlags ( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById ( R.id.opencv_tutorial_activity_surface_view );
        save_button = (Button) findViewById ( R.id.save_button );
        highlight_button = (Switch) findViewById ( R.id.highlight_button );
        show_object = (Switch) findViewById ( R.id.show_object );
        highlighted = highlight_button.isChecked ();
        show_object_status = show_object.isChecked ();

        save_button.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                save = true;
                Toast.makeText ( getApplicationContext (), "Saving..", Toast.LENGTH_SHORT ).show ();
            }
        } );
        highlight_button.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                if (highlight_button.isChecked ()) {
                    Toast.makeText ( getApplicationContext (), "Highlighting color",
                            Toast.LENGTH_SHORT ).show ();
                    highlighted = true;
                }
                else {
                    Toast.makeText ( getApplicationContext (), "Not highlighting!",
                            Toast.LENGTH_SHORT ).show ();
                    highlighted = false;
                }
            }
        } );
        show_object.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                if (show_object.isChecked ()) {
                    show_object_status = true;
                    Toast.makeText ( getApplicationContext (), "Objects boxed",
                            Toast.LENGTH_SHORT ).show ();
                }
                else {
                    show_object_status = false;
                    Toast.makeText ( getApplicationContext (), "Objects not displayed",
                            Toast.LENGTH_SHORT ).show ();
                }
            }
        } );
        mOpenCvCameraView.setVisibility ( SurfaceView.VISIBLE );
        mOpenCvCameraView.setCvCameraViewListener ( this );
    }

    @Override
    public void onPause() {
        super.onPause ();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView ();
    }

    @Override
    public void onResume() {
        super.onResume ();
        if (!OpenCVLoader.initDebug ()) {
            OpenCVLoader.initAsync ( OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback );
        } else {
            mLoaderCallback.onManagerConnected ( LoaderCallbackInterface.SUCCESS );
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy ();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView ();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat ();
    }

    public void onFilterRadioClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked ();

        // Check which radio button was clicked
        switch (view.getId ()) {
            case R.id.yellow:
                if (checked)
                    highlight_mask = YELLOW;
                break;
            case R.id.cyan:
                if (checked)
                    highlight_mask = CYAN;
                break;
            case R.id.magenta:
                if (checked)
                    highlight_mask = MAGENTA;
                break;
            case R.id.none:
                if (checked)
                    highlight_mask = WHITE;
                break;
        }
    }

    public void onHighlightRadioClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked ();

        // Check which radio button was clicked
        switch (view.getId ()) {
            case R.id.red:
                if (checked) {
                    color = 0;
                    Toast.makeText ( getApplicationContext (), "Red selected", Toast.LENGTH_SHORT ).show ();
                }
                break;
            case R.id.green:
                if (checked) {
                    color = 1;
                    Toast.makeText ( getApplicationContext (), "Green selected", Toast.LENGTH_SHORT ).show ();
                }
                break;
            case R.id.blue:
                if (checked) {
                    color = 2;
                    Toast.makeText ( getApplicationContext (), "Blue selected", Toast.LENGTH_SHORT ).show ();
                }
                break;
            case R.id.none_0:
                if (checked)
                    color = -1;
                break;
        }
    }


    @Override
    public void onCameraViewStopped() {
        mRgba.release ();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba ();
        mHSV = new Mat();
        Imgproc.cvtColor ( mRgba, mHSV, Imgproc.COLOR_RGB2HSV );
        return detectColor ();
    }


    @Override
    public void onStart() {
        super.onStart ();

    }

    @Override
    public void onStop() {
        super.onStop ();

    }

    private Mat detectColor() {

        Scalar boxColor = new Scalar ( 0, 0, 0 );

        switch (color) {
            case 0:
                Mat mask1 = new Mat ();
                Mat mask2 = new Mat ();
                Core.inRange ( mHSV, new Scalar ( 0, 150, 190 ),
                        new Scalar ( 30, 255, 255 ), mask1 );
                Core.inRange ( mHSV, new Scalar ( 155, 150, 190 ),
                        new Scalar ( 180, 255, 255 ), mask2 );
                boxColor = RED;
                Core.bitwise_or ( mask1, mask2, mHSV );
                mask1.release ();
                mask2.release ();
                break;

            case 1:
                Core.inRange ( mHSV, new Scalar ( 45, 70, 100 ),
                        new Scalar ( 90, 255, 255 ), mHSV );
                boxColor = GREEN;
                break;
            case 2:
                Core.inRange ( mHSV, new Scalar ( 80, 158, 124 ),
                        new Scalar ( 138, 255, 255 ), mHSV );
                boxColor = BLUE;
                break;
            case -1:

        }
        if (color >= 0) {
            highlightObjects();
            if (show_object_status)
                boxObjects(boxColor);
        }

        if (save)
            saveImage();

        mHSV.release ();
        return mRgba;
    }

    private void highlightObjects(){
        if (highlighted || highlight_mask != WHITE) {
            //https://docs.opencv.org/3.2.0/d0/d86/tutorial_py_image_arithmetics.html
            Mat mask = new Mat ();
            mHSV.copyTo ( mask );//mask obj in white
            Mat inv_mask = new Mat ();
            Core.bitwise_not ( mask, inv_mask );//obj in black
            Mat original_gray = new Mat ();
            Mat background = new Mat ();
            Mat foreground = new Mat ();

            //add_filter
            if (highlight_mask != WHITE){
                if (highlighted) {
                    Imgproc.cvtColor ( mRgba, original_gray, Imgproc.COLOR_RGB2GRAY );  //gray original image
                    Imgproc.cvtColor ( original_gray, original_gray, Imgproc.COLOR_GRAY2RGBA );  //change depth to 4
                } else {
                    mRgba.copyTo ( original_gray );  //okay not really gray
                }
                original_gray.copyTo ( background );
                mRgba.setTo ( highlight_mask, mask );
                Core.bitwise_and ( mRgba, mRgba, foreground, mask );
                Core.addWeighted ( background, 0.85, foreground, 0.2,0, mRgba );
            }
            else {
                if (highlighted) {
                    Imgproc.cvtColor ( mRgba, original_gray, Imgproc.COLOR_RGB2GRAY );  //gray original image
                    Imgproc.cvtColor ( original_gray, original_gray, Imgproc.COLOR_GRAY2RGBA );  //change depth to 4
                } else {
                    mRgba.copyTo ( original_gray );  //okay not really gray
                }
                Core.bitwise_and ( original_gray, original_gray, background, inv_mask );
                Core.bitwise_and ( mRgba, mRgba, foreground, mask );
                Core.add ( background, foreground, mRgba );
            }
            mask.release ();
            inv_mask.release ();
            original_gray.release ();
            background.release ();
            foreground.release ();
        }
    }


    private void boxObjects(Scalar boxColor){
        {
            Mat struct_element = Imgproc.getStructuringElement ( Imgproc.MORPH_RECT, new Size ( 5, 5 ) );
            Mat labels = new Mat (), stats = new Mat (), centroids = new Mat ();

            //Opening and Closing Morphological operation
            Imgproc.erode ( mHSV, mHSV, struct_element );
            Imgproc.dilate ( mHSV, mHSV, struct_element );
            Imgproc.dilate ( mHSV, mHSV, struct_element );
            Imgproc.erode ( mHSV, mHSV, struct_element );
            //http://answers.opencv.org/question/96443/find-rectangle-from-image-in-android/
            int n = Imgproc.connectedComponentsWithStats ( mHSV, labels, stats, centroids );

            Imgproc.cvtColor ( mHSV, mHSV, Imgproc.COLOR_GRAY2RGB );

            for (int i = 0; i < n; i++) {
                double[] left = stats.get ( i, Imgproc.CC_STAT_LEFT );
                double[] top = stats.get ( i, Imgproc.CC_STAT_TOP );
                double[] width = stats.get ( i, Imgproc.CC_STAT_WIDTH );
                double[] height = stats.get ( i, Imgproc.CC_STAT_HEIGHT );
                double[] area = stats.get ( i, Imgproc.CC_STAT_AREA );
                //outer rect area = 414720
                if ((area[0] < 200000) && (300 < area[0]))
                    Imgproc.rectangle ( mRgba, new Point ( left[0], top[0] ),
                            new Point ( left[0] + width[0], top[0] + height[0] ), boxColor, 5 );
            }
            labels.release ();
            centroids.release ();
            stats.release ();
        }
    }

    private void saveImage(){
        Bitmap temp = null;
        save = false;
        try {
            temp = Bitmap.createBitmap ( mRgba.cols (), mRgba.rows (), Bitmap.Config.ARGB_8888 );
            Utils.matToBitmap ( mRgba, temp );
        } catch (CvException e) {
            e.printStackTrace ();
        }

        FileOutputStream fp = null;
        String filename = new SimpleDateFormat ("yyyyMMddHHmmssSSS'.png'").format(new Date ());

        File sd = new File ( Environment.getExternalStorageDirectory () + "/MyApplication" );
        boolean success = true;
        if (!sd.exists ()) {
            success = sd.mkdir ();
        }

        if (success) {
            File dest = new File ( sd, filename );
            try {
                fp = new FileOutputStream ( dest );
                temp.compress ( Bitmap.CompressFormat.PNG, 100, fp );
            } catch (Exception e) {
                e.printStackTrace ();
            } finally {
                try {
                    if (fp != null) {
                        fp.close ();
                    }
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            }
        }
    }

}
