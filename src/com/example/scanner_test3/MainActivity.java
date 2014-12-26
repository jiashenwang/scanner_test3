package com.example.scanner_test3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;


public class MainActivity extends ActionBarActivity {

	private static int TAKE_PICTURE = 1;
	private Uri imageUri;
	ImageView imageView, debug_imageView;
	Button change;
	private static boolean flag = true;   
    private static boolean isFirst = true;
    Bitmap srcBitmap;  
    Bitmap processBitmap;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS ) {
            	Log.i("~~~~~~~", "Load successful!");
            } else {
                super.onManagerConnected(status);
                Log.i("~~~~~~~", "Load failed!");
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button camera = (Button) findViewById(R.id.camera);
        camera.setOnClickListener(cameraListener);
        imageView = (ImageView)findViewById(R.id.image);
        debug_imageView = (ImageView)findViewById(R.id.debug_image);
        change = (Button) findViewById(R.id.change);
        
    }
    
	@Override
	protected void onResume(){
	    Log.i("~~~~", "Called onResume");
	    super.onResume();
	    
	    Log.i("~~~~", "Trying to load OpenCV library");
	    if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, this, mLoaderCallback))
	    {
	        Log.e("~~~~", "Cannot connect to OpenCV Manager");
	    }
	      
	}
    
    private OnClickListener cameraListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			takePhoto(v);
			
			debug_imageView.setVisibility(View.GONE);
		}
    };

	protected void takePhoto(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/scanner_test2_pic.jpg");
		imageUri = Uri.fromFile(photo);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intent, TAKE_PICTURE);
		
		
	}
	
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // call the parent
        super.onActivityResult(requestCode, resultCode, data);
        
        imageView.setVisibility(View.VISIBLE);
        debug_imageView.setVisibility(View.VISIBLE);
        change.setVisibility(View.VISIBLE);
        isFirst = true;
        showPic();
        imageView.setImageBitmap(srcBitmap); 
        change.setOnClickListener(new ProcessClickListener());
    } 

	private class ProcessClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
            if(isFirst)  
            {  
            	processImage();  
                isFirst = false;  
            }
            if(flag){  
                imageView.setImageBitmap(processBitmap);  
                change.setText("Check Original");  
                flag = false;  
            } 
            else{  
            	imageView.setImageBitmap(srcBitmap);  
                change.setText(" change ");  
                flag = true;  
            } 
		}
		
	}
	
	public void showPic(){
		srcBitmap = BitmapFactory.decodeFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        		+"/scanner_test2_pic.jpg");
	}
	public void processImage(){  
        Mat rgbMat = new Mat();  
    	int dst_width = 1000;
        int dst_height = 600;
        
        // find the img file from cell-phone
        srcBitmap = BitmapFactory.decodeFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        		+"/scanner_test2_pic.jpg");

        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B. 
        
        Imgproc.resize(rgbMat, rgbMat, new Size(dst_width, dst_height));
        processBitmap = Bitmap.createBitmap((int)dst_width,(int)dst_height, Config.RGB_565);
        
        ArrayList<List<Point>> squares = new ArrayList<List<Point>>();
        ArrayList<List<Point>> largest_square = new ArrayList<List<Point>>();
        squares = (ArrayList<List<Point>>) find_squares(rgbMat).clone();
        largest_square = (ArrayList<List<Point>>) find_largest_square(squares).clone();
        
        Mat debugMat=rgbMat.clone();
        
        if(largest_square.size()!=0){
            Core.line(debugMat, largest_square.get(0).get(0), largest_square.get(0).get(0), new Scalar(255,0,0), 10);
            Core.line(debugMat, largest_square.get(0).get(1), largest_square.get(0).get(1), new Scalar(255,0,0), 20);
            Core.line(debugMat, largest_square.get(0).get(2), largest_square.get(0).get(2), new Scalar(255,0,0), 30);
            Core.line(debugMat, largest_square.get(0).get(3), largest_square.get(0).get(3), new Scalar(255,0,0), 40);
        }

        
        if(largest_square.size()==0){
        	Utils.matToBitmap(rgbMat, processBitmap);
        }else{
        	Point leftTop=new Point(0,0);
        	Point leftBot = new Point(0,0);
        	Point rightTop = new Point(0,0);
        	Point rightBot = new Point(0,0);
        	double maxSum=Double.NEGATIVE_INFINITY, 
        			minSum = Double.POSITIVE_INFINITY,
        			maxDiff = Double.NEGATIVE_INFINITY, 
        			minDiff = Double.POSITIVE_INFINITY;
        	
        	for(int i=0; i<largest_square.get(0).size();i++){
              	if(largest_square.get(0).get(i).x+largest_square.get(0).get(i).y <= minSum){
              		minSum = largest_square.get(0).get(i).x+largest_square.get(0).get(i).y;
              		leftTop.x = largest_square.get(0).get(i).x;
              		leftTop.y = largest_square.get(0).get(i).y;
              	}
              	if(largest_square.get(0).get(i).x+largest_square.get(0).get(i).y >= maxSum){
              		maxSum = largest_square.get(0).get(i).x+largest_square.get(0).get(i).y;
              		rightBot.x = largest_square.get(0).get(i).x;
              		rightBot.y = largest_square.get(0).get(i).y;
              	}
              	if(largest_square.get(0).get(i).x-largest_square.get(0).get(i).y <= minDiff){
              		minDiff = largest_square.get(0).get(i).x-largest_square.get(0).get(i).y;
              		leftBot.x = largest_square.get(0).get(i).x;
              		leftBot.y = largest_square.get(0).get(i).y;
              	}
              	if(largest_square.get(0).get(i).x-largest_square.get(0).get(i).y >= maxDiff){
              		maxDiff = largest_square.get(0).get(i).x-largest_square.get(0).get(i).y;
              		rightTop.x = largest_square.get(0).get(i).x;
              		rightTop.y = largest_square.get(0).get(i).y;
              	}
        	}        	
					
            Mat src_mat=new Mat(4,1,CvType.CV_32FC2);
            Mat dst_mat=new Mat(4,1,CvType.CV_32FC2);
            src_mat.put(0,0,leftTop.x,leftTop.y,
            		rightTop.x, rightTop.y, 
            		leftBot.x, leftBot.y, 
            		rightBot.x, rightBot.y );
            
            dst_mat.put(0,0, 0,0,dst_width,0, 0,dst_height, dst_width,dst_height);
            Mat tempMat = Imgproc.getPerspectiveTransform(src_mat, dst_mat);
            Mat dstMat=rgbMat.clone();
            Imgproc.warpPerspective(rgbMat, dstMat, tempMat, new Size(dst_width,dst_height));
            Utils.matToBitmap(dstMat, processBitmap);
            
        }
       
        
        Bitmap debug_bm = Bitmap.createBitmap((int)dst_width,(int)dst_height, Config.RGB_565);
		Utils.matToBitmap(debugMat, debug_bm);
        debug_imageView.setImageBitmap(debug_bm);
        Log.i("~~~~~~~", "Picture process sucess...");  
    }

	private ArrayList<List<Point>> find_largest_square(ArrayList<List<Point>> squares) {
		// TODO Auto-generated method stub
		ArrayList<List<Point>> largest_squares = new ArrayList<List<Point>>();
	    if (squares.size()==0)
	    {
	        // no squares detected
	        return largest_squares;
	    }else{
	        int max_width = 0;
	        int max_height = 0;
	        int max_square_idx = 0;
	        for (int i = 0; i < squares.size(); i++){

				Rect rectangle = Imgproc.boundingRect(new MatOfPoint(squares.get(i).get(0),
						squares.get(i).get(1),squares.get(i).get(2),squares.get(i).get(3)));
				
				if ((rectangle.width >= max_width) && (rectangle.height >= max_height))
		        {
		            max_width = rectangle.width;
		            max_height = rectangle.height;
		            max_square_idx = i;
		        }
	        }
	        largest_squares.add(squares.get(max_square_idx));
			return largest_squares;
	    }
	}

	private ArrayList<List<Point>> find_squares(Mat rgbMat) {
		// TODO Auto-generated method stub
		ArrayList<List<Point>> squares = new ArrayList<List<Point>>();
		
		Mat blurred_1 = new Mat(); 
		Mat gray_0 = new Mat();
		Mat gray = new Mat();
		Imgproc.medianBlur(rgbMat, blurred_1, 9);
		Imgproc.cvtColor(blurred_1, gray_0, Imgproc.COLOR_RGB2GRAY);
		
		List<Mat> blurred=new ArrayList<Mat>();
		List<Mat> gray0=new ArrayList<Mat>();
		blurred.add(blurred_1);
		gray0.add(gray_0);
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		
	    // find squares in every color plane of the image
	    for (int c = 0; c < 3; c++){
	    	int ch[] = {c, 0};
	    	MatOfInt fromto = new MatOfInt(ch);
	    	Core.mixChannels(blurred, gray0, fromto);
	    	
		    int threshold_level = 2;
		    for (int l = 0; l < threshold_level; l++){
		    	if (l == 0){
		    		Imgproc.Canny(gray0.get(0), gray, 10, 20, 3, false);
		    		Imgproc.dilate(gray, gray, new Mat(), new Point(-1,-1),1);
		    	}else{
		    		//gray = gray0.get(0) >= (l+1) * 255 / threshold_level;
		    	}
		    	Mat hierarchy = new Mat();
		    	Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		    	
		    	//List<MatOfPoint> approx = new ArrayList<MatOfPoint>();
	    		MatOfPoint2f approx = new MatOfPoint2f();
		    	for (int i = 0; i < contours.size(); i++){
					Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approx, 
							Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true)*0.02, true);
					
					if(approx.toList().size()==4 && 
							Math.abs(Imgproc.contourArea(approx)) > 1000 &&
							Imgproc.isContourConvex(new MatOfPoint(approx.toArray()))){
						double maxCosine = 0;
					
						for (int j = 2; j < 5; j++){
							double cosine = Math.abs(angle(approx.toList().get(j%4), approx.toList().get(j-2), approx.toList().get(j-1)));
							maxCosine = Math.max(maxCosine, cosine);
						}
						if (maxCosine < 0.3){
	                        squares.add(approx.toList());
						}
					}
		    	} 
		    }
	    }
	    
		
		
		
		return squares;
	}
	
	double angle( Point pt1, Point pt2, Point pt0 ) {
	    double dx1 = pt1.x - pt0.x;
	    double dy1 = pt1.y - pt0.y;
	    double dx2 = pt2.x - pt0.x;
	    double dy2 = pt2.y - pt0.y;
	    return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
	}

}
