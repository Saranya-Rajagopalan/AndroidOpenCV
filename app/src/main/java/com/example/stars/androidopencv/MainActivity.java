package com.example.stars.androidopencv;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import static org.opencv.features2d.Features2d.DRAW_RICH_KEYPOINTS;

    public class MainActivity extends AppCompatActivity implements OnTouchListener, CvCameraViewListener2 {
        private static final int REQUEST_CODE = 100;
        private static final Scalar RED = new Scalar ( 255, 0 ,0 );
        private static final Scalar GREEN = new Scalar ( 0, 255 ,0 );
        private static final Scalar BLUE = new Scalar ( 0, 0 ,255 );
        TextView touch_coordinates;
        TextView touch_color;
        Button red_button, green_button, blue_button;
        FeatureDetector detector;
        DescriptorExtractor descriptor;
        DescriptorMatcher matcher;
        Mat descriptor1, descriptor2;
        MatOfKeyPoint keypoints1, keypoints2;
        int color = -1;
        private CameraBridgeViewBase mOpenCvCameraView;
        private Mat mRgba, mRef;
        private Scalar mBlobColorRgba;
        private Scalar mBlobColorHsv;
        private boolean touched;
        private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        mOpenCvCameraView.enableView();
                        mOpenCvCameraView.setOnTouchListener(MainActivity.this);

                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            //https://stackoverflow.com/questions/38552144/how-get-permission-for-camera-in-android-specifically-marshmallow
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE);
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_tutorial_activity_surface_view);
            touch_coordinates = (TextView) findViewById(R.id.touch_coordinates);
            touch_color = (TextView) findViewById(R.id.touch_color);
            red_button = (Button) findViewById(R.id.red_button);
            green_button = (Button) findViewById(R.id.green_button);
            blue_button = (Button) findViewById(R.id.blue_button);
            red_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    color = 0;
                    Toast.makeText(getApplicationContext(),"Red clicked",Toast.LENGTH_SHORT).show();
                }
            });
            green_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    color = 1;
                    Toast.makeText(getApplicationContext(),"Green clicked",Toast.LENGTH_SHORT).show();
                }
            });
            blue_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    color = 2;
                    Toast.makeText(getApplicationContext(),"Blue clicked",Toast.LENGTH_SHORT).show();
                }
            });
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            if (mOpenCvCameraView != null)
                mOpenCvCameraView.disableView();
        }

        @Override
        public void onResume() {
            super.onResume();
            if (!OpenCVLoader.initDebug()) {
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
            } else {
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mOpenCvCameraView != null)
                mOpenCvCameraView.disableView();
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            double x, y;

            int cols = mRgba.cols();
            int rows = mRgba.rows();

            double yLow = (double) mOpenCvCameraView.getHeight() * 0.2401961;
            double yHigh = (double) mOpenCvCameraView.getHeight() * 0.7696078;
            double xScale = (double) cols / (double) mOpenCvCameraView.getWidth();
            double yScale = (double) rows / (yHigh - yLow);
            x = event.getX();
            y = event.getY();
            y = y - yLow;
            x = x * xScale;
            y = y * yScale;
            if (((x < 0) || (y < 0)) || ((x > cols) || (y > rows))) return false;
            touch_coordinates.setText("X: " + Double.valueOf(x) + ", Y: " + Double.valueOf(y));

            Rect touchedRect = new Rect();

            touchedRect.x = (int) x;
            touchedRect.y = (int) y;

            touchedRect.width = 8;
            touchedRect.height = 8;

            Mat touchedRegionRgba = mRgba.submat(touchedRect);

            Mat touchedRegionHsv = new Mat();
            Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

            mBlobColorHsv = Core.sumElems(touchedRegionHsv);
            int pointCount = touchedRect.width * touchedRect.height;
            for (int i = 0; i < mBlobColorHsv.val.length; i++)
                mBlobColorHsv.val[i] /= pointCount;

            mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv);

            touch_color.setText("Color: %" + String.format("%02X", (int) mBlobColorRgba.val[0])
                    + String.format("%02X", (int) mBlobColorRgba.val[1])
                    + String.format("%02X", (int) mBlobColorRgba.val[2]));

            touch_color.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],
                    (int) mBlobColorRgba.val[1],
                    (int) mBlobColorRgba.val[2]));

            touch_coordinates.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],
                    (int) mBlobColorRgba.val[1],
                    (int) mBlobColorRgba.val[2]));

            touched = true;
            return false;
        }

        private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
            Mat pointMatRgba = new Mat();
            Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
            Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

            return new Scalar(pointMatRgba.get(0, 0));
        }

        @Override
        public void onCameraViewStarted(int width, int height) {
            mRgba = new Mat();
            mRef = new Mat();
            descriptor1 = new Mat();
            descriptor2 = new Mat();
            keypoints1 = new MatOfKeyPoint();
            keypoints2 = new MatOfKeyPoint();
            mBlobColorRgba = new Scalar(255);
            mBlobColorHsv = new Scalar(255);
            touched = false;
            try {
                init();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void init() throws IOException {

            detector = FeatureDetector.create(FeatureDetector.ORB);
            descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
            matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);


            AssetManager assetManager = getAssets();
            InputStream input_stream;
            input_stream = assetManager.open("image2.jpg");
            Bitmap bit = BitmapFactory.decodeStream(input_stream);
            Utils.bitmapToMat(bit, mRef);
            Imgproc.cvtColor(mRef, mRef, Imgproc.COLOR_RGB2GRAY);
            mRef.convertTo(mRef, 0);
            detector.detect(mRef, keypoints1);
            descriptor.compute(mRef, keypoints1, descriptor1);

        }

        @Override
        public void onCameraViewStopped() {
            mRgba.release();
        }

        public Mat detectColor() {

            Mat struct_element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
            Mat labels = new Mat(), stats = new Mat(), centroids = new Mat();

            Mat inputHSV = new Mat();
            Mat mask1 = new Mat();
            Mat mask2 = new Mat();
            Scalar boxColor;
            Imgproc.cvtColor(mRgba, inputHSV, Imgproc.COLOR_RGB2HSV);
            switch(color) {
                case 0:
                    Core.inRange(inputHSV, new Scalar(0, 20, 70),
                            new Scalar(40,  255, 255), mask1);
                    Core.inRange(inputHSV, new Scalar(220, 20, 70),
                            new Scalar(255,  255, 255), mask2);
                    boxColor = RED;
                    Core.bitwise_or ( mask1, mask2, inputHSV );
                    break;

                case 1:
                    Core.inRange(inputHSV, new Scalar(30, 70, 100),
                            new Scalar(90,  255, 255), inputHSV);
                    boxColor = GREEN;
                    break;
                case 2:
                    Core.inRange(inputHSV, new Scalar(75, 158, 124),
                            new Scalar(138,  255, 255), inputHSV);
                    boxColor = BLUE;
                    break;
                default:
                    return mRgba;
            }


            //Opening and Closing Morphological operation
            Imgproc.erode(inputHSV, inputHSV, structelement);
            Imgproc.dilate(inputHSV, inputHSV, structelement);
            Imgproc.dilate(inputHSV, inputHSV, structelement);
            Imgproc.erode(inputHSV, inputHSV, structelement);

            Core.bitwise_not(inputHSV, inputHSV);

            //http://answers.opencv.org/question/96443/find-rectangle-from-image-in-android/
            int n = Imgproc.connectedComponentsWithStats(inputHSV, labels, stats, centroids);

            Imgproc.cvtColor(inputHSV,inputHSV,Imgproc.COLOR_GRAY2RGB);
            for (int i = 0; i < n; i++){
                double[] left = stats.get(i, Imgproc.CC_STAT_LEFT);
                double[] top = stats.get(i, Imgproc.CC_STAT_TOP);
                double[] width = stats.get(i, Imgproc.CC_STAT_WIDTH);
                double[] height = stats.get(i, Imgproc.CC_STAT_HEIGHT);
                double[] area = stats.get(i, Imgproc.CC_STAT_AREA);
                //outer rect area = 414720
                if(area[0] < 200000 && area[0] > 100)
                    Imgproc.rectangle(mRgba, new Point(left[0],top[0]), new Point(left[0]+width[0], top[0]+height[0]), boxColor, 5);
            }

            return mRgba;
        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            mRgba = inputFrame.rgba();
            return detectColor();
        }


        @Override
        public void onStart() {
            super.onStart();

        }

        @Override
        public void onStop() {
            super.onStop();

        }
    }

