package com.example.stars.androidopencv;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements OnTouchListener, CvCameraViewListener2 {
    private static final int REQUEST_CODE = 100;
    private static final Scalar RED = new Scalar ( 255, 0, 0 );
    private static final Scalar GREEN = new Scalar ( 0, 255, 0 );
    private static final Scalar BLUE = new Scalar ( 0, 0, 255 );
    private TextView touch_coordinates, touch_color;
    private Button red_button, green_button, blue_button, pick_button;
    private Switch highlight_button;
    int color = -1;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;

    private boolean highlight = false;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback ( this ) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView ();
                    mOpenCvCameraView.setOnTouchListener ( MainActivity.this );

                }
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
        //https://stackoverflow.com/questions/38552144/how-get-permission-for-camera-in-android-specifically-marshmallow
        if (ContextCompat.checkSelfPermission ( getApplicationContext (), Manifest.permission.CAMERA )
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions ( this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE );
        }
        getWindow ().addFlags ( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById ( R.id.opencv_tutorial_activity_surface_view );
        touch_coordinates = (TextView) findViewById ( R.id.touch_coordinates );
        touch_color = (TextView) findViewById ( R.id.touch_color );
        red_button = (Button) findViewById ( R.id.red_button );
        green_button = (Button) findViewById ( R.id.green_button );
        blue_button = (Button) findViewById ( R.id.blue_button );
        pick_button = (Button) findViewById ( R.id.pick_button );
        highlight_button = (Switch) findViewById ( R.id.highlight_button );
        red_button.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                color = 0;
                Toast.makeText ( getApplicationContext (), "Red clicked", Toast.LENGTH_SHORT ).show ();
            }
        } );
        green_button.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                color = 1;
                Toast.makeText ( getApplicationContext (), "Green clicked", Toast.LENGTH_SHORT ).show ();
            }
        } );
        blue_button.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                color = 2;
                Toast.makeText ( getApplicationContext (), "Blue clicked", Toast.LENGTH_SHORT ).show ();
            }
        } );
        pick_button.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                color = 3;
                Toast.makeText ( getApplicationContext (), "Alright! Pick a color from the scene!",
                        Toast.LENGTH_SHORT ).show ();
            }
        } );
        highlight_button.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                highlight = !highlight;
                if(highlight)
                    Toast.makeText ( getApplicationContext (), "Highlighting!",
                        Toast.LENGTH_SHORT ).show ();
                else
                    Toast.makeText ( getApplicationContext (), "Not highlighting!",
                            Toast.LENGTH_SHORT ).show ();
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
    public boolean onTouch(View v, MotionEvent event) {
        double x, y;

        int cols = mRgba.cols ();
        int rows = mRgba.rows ();

        double yLow = (double) mOpenCvCameraView.getHeight () * 0.2401961;
        double yHigh = (double) mOpenCvCameraView.getHeight () * 0.7696078;
        double xScale = (double) cols / (double) mOpenCvCameraView.getWidth ();
        double yScale = (double) rows / (yHigh - yLow);
        x = event.getX ();
        y = event.getY ();
        y = y - yLow;
        x = x * xScale;
        y = y * yScale;
        if (((x < 0) || (y < 0)) || ((x > cols) || (y > rows))) return false;
        touch_coordinates.setText ( "X: " + Double.valueOf ( x ) + ", Y: " + Double.valueOf ( y ) );

        Rect touchedRect = new Rect ();

        touchedRect.x = (int) x;
        touchedRect.y = (int) y;

        touchedRect.width = 8;
        touchedRect.height = 8;

        Mat touchedRegionRgba = mRgba.submat ( touchedRect );

        Mat touchedRegionHsv = new Mat ();
        Imgproc.cvtColor ( touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL );

        mBlobColorHsv = Core.sumElems ( touchedRegionHsv );
        int pointCount = touchedRect.width * touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = convertScalarHsv2Rgba ( mBlobColorHsv );

        touch_color.setText ( "Color: #" + String.format ( "%02X", (int) mBlobColorRgba.val[0] )
                + String.format ( "%02X", (int) mBlobColorRgba.val[1] )
                + String.format ( "%02X", (int) mBlobColorRgba.val[2] ) );

        touch_color.setTextColor ( Color.rgb ( (int) mBlobColorRgba.val[0],
                (int) mBlobColorRgba.val[1],
                (int) mBlobColorRgba.val[2] ) );

        touch_coordinates.setTextColor ( Color.rgb ( (int) mBlobColorRgba.val[0],
                (int) mBlobColorRgba.val[1],
                (int) mBlobColorRgba.val[2] ) );

        return false;
    }

    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat ();
        Mat pointMatHsv = new Mat ( 1, 1, CvType.CV_8UC3, hsvColor );
        Imgproc.cvtColor ( pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4 );
        return new Scalar ( pointMatRgba.get ( 0, 0 ) );
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat ();
        mBlobColorRgba = new Scalar ( 255 );
        mBlobColorHsv = new Scalar ( 255 );
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release ();
    }

    public Mat detectColor() {

        Mat struct_element = Imgproc.getStructuringElement ( Imgproc.MORPH_RECT, new Size ( 5, 5 ) );
        Mat labels = new Mat (), stats = new Mat (), centroids = new Mat ();

        Mat inputHSV = new Mat ();
        Mat mask1 = new Mat ();
        Mat mask2 = new Mat ();
        Scalar boxColor;
        Imgproc.cvtColor ( mRgba, inputHSV, Imgproc.COLOR_RGB2HSV );
        switch (color) {
            case 0:
                Core.inRange ( inputHSV, new Scalar ( 0, 190, 190 ),
                        new Scalar ( 15, 255, 255 ), mask1 );
                Core.inRange ( inputHSV, new Scalar ( 160, 190, 190 ),
                        new Scalar ( 180, 255, 255 ), mask2 );
                boxColor = RED;
                Core.bitwise_or ( mask1, mask2, inputHSV );
                mask1.release ();
                mask2.release ();
                break;

            case 1:
                Core.inRange ( inputHSV, new Scalar ( 30, 70, 100 ),
                        new Scalar ( 90, 255, 255 ), inputHSV );
                boxColor = GREEN;
                break;
            case 2:
                Core.inRange ( inputHSV, new Scalar ( 75, 158, 124 ),
                        new Scalar ( 138, 255, 255 ), inputHSV );
                boxColor = BLUE;
                break;
            case 3:
                int h = (int) mBlobColorHsv.val[0];
                int s = (int) mBlobColorHsv.val[1];
                int v = (int) mBlobColorHsv.val[2];
                Core.inRange ( inputHSV, new Scalar ( h-20, s-10, v-10 ),
                        new Scalar ( h+40, 255, 255 ), inputHSV );
                boxColor = convertScalarHsv2Rgba ( new Scalar ( h,s,v ) );
                break;
            default:
                return mRgba;
        }

        if(highlight){
            //https://docs.opencv.org/3.2.0/d0/d86/tutorial_py_image_arithmetics.html
            Mat mask = inputHSV;
            Mat inv_mask = new Mat();
            Core.bitwise_not (mask, inv_mask);
            Mat mGray = new Mat();
            Imgproc.cvtColor ( mRgba, mGray, Imgproc.COLOR_RGB2GRAY );
            Imgproc.cvtColor ( mGray, mGray, Imgproc.COLOR_GRAY2RGBA);
            Mat background = new Mat();
            Mat foreground = new Mat();
            Core.bitwise_and ( mGray, mGray, background, inv_mask );
            Core.bitwise_and ( mRgba, mRgba, foreground, mask );
            Core.add ( background, foreground, mRgba );

            mask.release ();
            inv_mask.release ();
            mGray.release ();
            background.release ();
            foreground.release ();
        }



        /*
        if (highlight){
            for(int i = 0; i <inputHSV.rows (); i++)
                for(int j = 0; j <inputHSV.cols(); j++){
                    if(inputHSV.get (i, j )[0]==1){
                        double[] value = mRgba.get (i,j);
                        double average = (value[0] + value[1] + value[2])/3;
                        value[0] =  average;
                        value[1] =  average;
                        value[2] =  average;
                        mRgba.put(i ,j ,value);
                    }
                }
        }
        */

        //Opening and Closing Morphological operation
        Imgproc.erode ( inputHSV, inputHSV, struct_element );
        Imgproc.dilate ( inputHSV, inputHSV, struct_element );
        Imgproc.dilate ( inputHSV, inputHSV, struct_element );
        Imgproc.erode ( inputHSV, inputHSV, struct_element );

        //http://answers.opencv.org/question/96443/find-rectangle-from-image-in-android/
        int n = Imgproc.connectedComponentsWithStats ( inputHSV, labels, stats, centroids );

        Imgproc.cvtColor ( inputHSV, inputHSV, Imgproc.COLOR_GRAY2RGB );

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

        inputHSV.release ();
        labels.release ();
        centroids.release ();
        stats.release ();
        return mRgba;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba ();
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
}

