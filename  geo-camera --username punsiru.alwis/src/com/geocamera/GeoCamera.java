package com.geocamera;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * 
 * @author Punsiru Alwis
 *
 */
public class GeoCamera extends MapActivity implements SurfaceHolder.Callback, OnClickListener{
	
	static final int FOTO_MODE = 0;
	
	private LocationManager locationManager;
	private LocationListener locationListener;
	private GeoPoint currentGeoPoint;
	private SurfaceView surefaceView;
	private SurfaceHolder surefaceHolder;
	private Camera camera;
	private String make;
	private String model;
	private String imei;
	
	private boolean previewRunning = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
        		WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // load the layout
        setContentView(R.layout.main);
        surefaceView = (SurfaceView) findViewById(R.id.surface_camera);
        surefaceView.setOnClickListener(this);
        surefaceHolder = surefaceView.getHolder();
        surefaceHolder.addCallback(this);
        surefaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);	
		
        // initiate a location listener
        locationListener = new LocationListener() {
			
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub
				
			}
			
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			public void onLocationChanged(Location location) {
				// TODO Update the Latitude and Longitude of the location
				initiateLocation();
			}
		};
		
		// initiate location manager
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        
        // initiate the location using GPS
        initiateLocation();
    }
    
    /*
     *  initiate auto focus
     */
    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){

		@Override
		public void onAutoFocus(boolean arg0, Camera arg1) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(), "'It is ready to take the photograph !!!", Toast.LENGTH_SHORT).show();
		}};
    
    /*
     * Called when GPS location is created
     */
    public void initiateLocation(){
    	setCurrentGeoPoint(new GeoPoint( 
        		(int)(locationManager.getLastKnownLocation(
        				LocationManager.GPS_PROVIDER).getLatitude()*1000000.0),
        		(int)(locationManager.getLastKnownLocation(
        				LocationManager.GPS_PROVIDER).getLongitude()*1000000.0)));
    }
    
    Camera.PictureCallback pictureCallBack = new Camera.PictureCallback() {
		
    /*
     * (non-Javadoc)
     * @see android.hardware.Camera.PictureCallback#onPictureTaken(byte[], android.hardware.Camera)
     * create new intent and store the image
     */
	public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			
			if(data != null){
				Intent imgIntent = new Intent();
				storeByteImage(data);
				camera.startPreview();
				setResult(FOTO_MODE, imgIntent);
			}
			
		}
	};

    /*
     * called when image is stored
     */
    public boolean storeByteImage(byte[] data){
    	// Create the <timestamp>.jpg file and modify the exif data
    	String filename = "/sdcard"+String.format("/%d.jpg", System.currentTimeMillis());
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			try {
				fileOutputStream.write(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileOutputStream.flush();
			fileOutputStream.close();
			ExifInterface exif = new ExifInterface(filename);
			createExifData(exif);
			exif.saveAttributes();
			return true;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
    
    /*
     * called when exif data profile is created
     */
    public void createExifData(ExifInterface exif){
    	// create a reference for Latitude and Longitude
    	double lat = currentGeoPoint.getLatitudeE6()/1000000.0;
    	if (lat < 0) {
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            lat = -lat;
        } else {
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
        }
    	
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
        		formatLatLongString(lat));
        
        double lon = currentGeoPoint.getLongitudeE6()/1000000.0;
        if (lon < 0) {
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            lon = -lon;
        } else {
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
        }
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
        		formatLatLongString(lon));
    	
    	try {
			exif.saveAttributes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		make = android.os.Build.MANUFACTURER; // get the make of the device
		model = android.os.Build.MODEL; // get the model of the divice
		
		exif.setAttribute(ExifInterface.TAG_MAKE, make);
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();
		exif.setAttribute(ExifInterface.TAG_MODEL, model+" - "+imei);
		
		exif.setAttribute(ExifInterface.TAG_DATETIME, (new Date(System.currentTimeMillis())).toString()); // set the date & time

    }
    
    /*
     * formnat the Lat Long values according to standard exif format
     */
    private static String formatLatLongString(double d) {
    	// format latitude and longitude according to exif format
    	StringBuilder b = new StringBuilder();
        b.append((int) d);
        b.append("/1,");
        d = (d - (int) d) * 60;
        b.append((int) d);
        b.append("/1,");
        d = (d - (int) d) * 60000;
        b.append((int) d);
        b.append("/1000");
        return b.toString();
      }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCurrentGeoPoint(GeoPoint currentGeoPoint) {
		this.currentGeoPoint = currentGeoPoint;
	}

	public GeoPoint getCurrentGeoPoint() {
		return currentGeoPoint;
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * called when clicked on the surface
	 */
	public void onClick(View v) {
		// TODO Auto-generated method stub
		camera.takePicture(null, pictureCallBack, pictureCallBack);
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		if(previewRunning){
			camera.stopPreview();
		}
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(width, height);
		camera.setParameters(parameters);
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		camera.startPreview();
		previewRunning = true;
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		camera = Camera.open();
		
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
	 * Called when it release the camera resource
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		camera.stopPreview();
		previewRunning = false;
		camera.release();	
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * Called when clicked on the menu button
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cameramenu, menu);
        return true;
    }

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * define menu actions
	 */
	public boolean onOptionsItemSelected(MenuItem item) { 
        switch (item.getItemId()) {
            case R.id.item01:    
            	// Toast.makeText(this, "You pressed Gallery!", Toast.LENGTH_LONG).show();	
            	startGallery();             
            	break;
            case R.id.item03:     
            	//Toast.makeText(this, "You pressed Exit!", Toast.LENGTH_LONG).show();		
            	System.exit(0);			
            	break;
        }
        return true;
    }
	
	/*
	 * Called when click on the Gallery menu
	 */
	private void startGallery(){
		Intent intent = new Intent();
		intent.setClass(GeoCamera.this, GeoGallery.class);
		startActivity(intent);
	}
}