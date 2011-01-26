package com.geocamera;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author Punsiru Alwis
 *
 */
public class GeoGallery extends Activity{
	static final int ID_JPGDIALOG = 0;
	static final int IMAGE_FACTOR = 2;
	static final int WIDTH = 136;
	static final int HEIGHT = 102;
	private static final String TAG = "Gallary View";
	
	private List<String> fileList;
	private BitmapFactory.Options bmOptions; 
	private ImageView imgView;
	private Bitmap bmp;
	private String currentFile;
	private String exifAttribute;
	private String geoCode;
	private GeoDegree geoDegree;
	private Dialog jpgDialog;
	private TextView exifText;
	private TextView geoText;
	private Gallery gallery;
	//private Location currentLocation;
	
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	 super.onCreate(savedInstanceState);
    	 // set gallery layout and find widgets
         setContentView(R.layout.gallery);
         gallery = (Gallery) findViewById(R.id.gallery); 
         imgView = (ImageView) findViewById(R.id.image);
         
         //	read SD card and create the fileList
         ReadSDCard(); 
         
         // set adapter to combine both gallery and images
         gallery.setAdapter(new ImageAdapter(this, fileList));
         
         // create the image
         bmOptions = new BitmapFactory.Options();
 		 bmOptions.inSampleSize = IMAGE_FACTOR;
         
 		 /*
 		  * set an item select listener for the gallery
 		  */
 		 gallery.setOnItemSelectedListener(new OnItemSelectedListener(){

 			 public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				// TODO call the function on item selected
 				setImageOnClick(arg0, arg1, arg2, arg3);
 				
 			}

 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				
 				
 			}
 			
 		 });
         
 		 /*
 		  * set an item click listener for the gallery
 		  */
 		 
 		 gallery.setOnItemClickListener(new OnItemClickListener() {

 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO call the function on item clicked
 				setImageOnClick(arg0, arg1, arg2, arg3);
 				
 			}
 		 });
 		   
 		 /*
 		  * set an item click listener for the image
 		  */
 		 
		 imgView.setOnClickListener(new OnClickListener() {
	 			
	 			public void onClick(View v) {
	 				// TODO execute on a click
	 				// create a file from the currently selected image
	 				File file = new File(currentFile);
	 				exifAttribute = null;
	 				// get the file extension
	 				String ext = currentFile.substring(currentFile.lastIndexOf('.')+1, currentFile.length());
	 					// check for a jpg/JPG file
	 					if(ext.equals("JPG")||ext.equals("jpg"))
	 					{	ExifInterface exif;
	 						try {
	 							// create an exif interface from the current file
	 							exif = new ExifInterface(file.toString());
	 							// extract exif data
	 							exifAttribute = getExif(exif);
	 							// create new geo point
	 							geoDegree = new GeoDegree(exif);
	 							handleReverseGeocodeClick();
	 							
	 						} catch (IOException e) {
	 							// TODO Auto-generated catch block
	 							
	 						}
	 					}
	 				
	 			}
					
	 		});
     
    }
    
    private void handleReverseGeocodeClick()
    {
    	if (this.geoDegree != null)
    	{
    		// Kickoff an asynchronous task to fire the reverse geocoding
    		// request off to google
    		ReverseGeocodeLookupTask task = new ReverseGeocodeLookupTask();
    		task.applicationContext = this;
    		task.execute();
    	}
    	
    }
    
    public class ReverseGeocodeLookupTask extends AsyncTask <Void, Void, String>
    {
    	private ProgressDialog dialog;
    	protected Context applicationContext;
    	
    	@Override
    	protected void onPreExecute()
    	{
    		this.dialog = ProgressDialog.show(applicationContext, "Please wait...connecting......", 
                    "Requesting reverse geocode lookup", true);
    	}
    	
		@Override
		protected String doInBackground(Void... params) 
		{
			String localityName = "No Internet Access";
			
			if (geoDegree != null)
			{
				localityName = GeoCoder.reverseGeocode(geoDegree);
			}
			
			return localityName;
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			this.dialog.cancel();
			geoCode = result;
			// show the dialog box
			
			showDialog(ID_JPGDIALOG);
			
		}
    }
    
    /**
     * Called when the menu button pressed
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gallerymenu, menu);
        return true;
    }
    
    /**
     * Called when click menu items
     */
    public boolean onOptionsItemSelected(MenuItem item) {
    	/*
    	 * Define menu actions
    	 */
        switch (item.getItemId()) {
            case R.id.item01:   
            	// Toast.makeText(this, "You pressed Show on Map!", Toast.LENGTH_LONG).show();
            	StartMapView();
            	break;
            case R.id.item03:     
            	//Toast.makeText(this, "You pressed Delete!", Toast.LENGTH_LONG).show();		
            	deleteCurrentFile(currentFile);			
            	break;
            	
        }
        
        return true;
    }
    
    /**
     * Called when confirmation dialog box displayed
     * @param currentFile, 
     */
    private void deleteCurrentFile(String currentFile){
    	AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
    	 
        // set the message to display
        alertbox.setMessage("Are you sure you want to delete this image?\n("+currentFile.substring(8)+")");

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            // do something when the button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
            	del();
            }
        });

        // set a negative/no button and create a listener
        alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {

            // do something when the button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });

        // display box
        alertbox.show();

    }
    
    /**
     * Called when file deleted
     */
    private void del(){
    	File file = new File(currentFile);
      	file.delete();
      	ReadSDCard();
      	gallery.setAdapter(new ImageAdapter(this, fileList));
    }
    
    /**
     * Called when dialog box is created
     */
    protected Dialog onCreateDialog(int id) {
 		jpgDialog = null;;
 		switch(id){
 		case ID_JPGDIALOG:

 			Context mContext = this;
 			jpgDialog = new Dialog(mContext);

 			jpgDialog.setContentView(R.layout.detaildialog);
 			exifText = (TextView) jpgDialog.findViewById(R.id.text);
 			
 			geoText = (TextView)jpgDialog.findViewById(R.id.geotext);
 			
 			Button okDialogButton = (Button)jpgDialog.findViewById(R.id.okdialogbutton);
 			okDialogButton.setOnClickListener(okDialogButtonOnClickListener);
 			
 			break;
 		default:
 			break;
 		}
 		return jpgDialog;
 		
 	}
    
    /**
      * Called when dialog is prepared
      */
    protected void onPrepareDialog(int id, Dialog dialog) {
 		// TODO Auto-generated method stub

 		switch(id){
 		case ID_JPGDIALOG:
 			dialog.setTitle("Image Details\n("+currentFile.substring(8)+")");
 			exifText.setText(exifAttribute); 
 			
 			if(geoDegree.isValid())
 			{
 				
 				geoText.setText(geoCode+"\n("+geoDegree.toString()+")");
 			}
 			else
 			{
 				geoText.setText("");
 			}
 			
 			break;
 		default:
 			break;
 		}
 	}

    private Button.OnClickListener okDialogButtonOnClickListener = new Button.OnClickListener(){

 		public void onClick(View v) {
 			// TODO hidden the dialog box
 			jpgDialog.dismiss();
 		}
     };
 
    /**
     * Combine all exif data
     * @param exif
     * @return string containing all exif data
     */
    private String getExif(ExifInterface exif){
    	String myAttribute="";
    	myAttribute += getTagString(ExifInterface.TAG_DATETIME, exif);
    	myAttribute += getTagString(ExifInterface.TAG_FLASH, exif);
    	myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE, exif);
    	myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif);
    	myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif);
    	myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif);
    	myAttribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
    	myAttribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
    	myAttribute += getTagString(ExifInterface.TAG_MAKE, exif);
    	myAttribute += getTagString(ExifInterface.TAG_MODEL, exif);
    	myAttribute += getTagString(ExifInterface.TAG_ORIENTATION, exif);
    	myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);
    	return myAttribute;
    }
     
    private String getTagString(String tag, ExifInterface exif)
     {
      return(tag + " : " + exif.getAttribute(tag) + "\n");
     }
     
    /**
     * Called when a gallery element clicked
     */
    private void setImageOnClick(AdapterView<?> arg0, View arg1,
 			int arg2, long arg3){
     	bmp = null;
     	currentFile = fileList.get(arg2);
 		bmp = BitmapFactory.decodeFile(fileList.get(arg2), bmOptions);
 		
 		imgView.setImageBitmap(bmp);
 	}
     
    protected void onResume() {
 		Log.e(TAG, "onResume");
 		super.onResume();
 	}

 	protected void onSaveInstanceState(Bundle outState) {
 		Log.e(TAG, "onSave");
 		super.onSaveInstanceState(outState);
 	}

 	protected void onStop() {
 		Log.e(TAG, "onStop");
 		super.onStop();
 	}
 	
    private void ReadSDCard(){
      fileList = new ArrayList<String>(); // initiate the filelist

      //It have to be matched with the directory in SDCard
      File f = new File("/sdcard/");
      File tempFile;

      File[] tempFileArray = f.listFiles(); //load all files to a file array

      for(int i=0; i<tempFileArray.length; i++)
      {
       tempFile = tempFileArray[i];
       /*filter and add all jpg files to an array*/
       if(tempFile.isFile()){
     	  fileList.add(tempFile.getPath());
       }
      }
     }

    /**
     * Create an Image Adapter to combine both image and gallery
     */
    public class ImageAdapter extends BaseAdapter {
         int mGalleryItemBackground;
         private Context mContext;
         private List<String> FileList;
         private Bitmap bitmp = null;
         private ImageView img = null;

         /**
          * 
          * @param c
          * @param fList
          */
         public ImageAdapter(Context c, List<String> fList) {
             mContext = c.getApplicationContext();
             FileList = fList;
             TypedArray a = obtainStyledAttributes(R.styleable.Theme);
             mGalleryItemBackground = a.getResourceId(
               R.styleable.Theme_android_galleryItemBackground,
               0);
             a.recycle();
         }

         public int getCount() {
             return fileList.size();
         }

         public Object getItem(int position) {
             return position;
         }

         public long getItemId(int position) {
             return position;
         }
         
         public View getView(int position, View convertView, ViewGroup parent) {
         	img = null;
         	bitmp = null;
         	
            img = new ImageView(mContext.getApplicationContext());
            bitmp = BitmapFactory.decodeFile(
            		FileList.get(position).toString());
            img.setImageBitmap(bitmp);
         
            img.setLayoutParams(new Gallery.LayoutParams(WIDTH, HEIGHT));
            img.setScaleType(ImageView.ScaleType.FIT_XY);
            img.setBackgroundResource(mGalleryItemBackground);
         
            return img;
         }
     }
    
    /**
     * Called when show on map clicked
     */
    private void StartMapView(){
 		Intent intent = new Intent();
 		intent.setClass(GeoGallery.this, GeoMap.class);
 		Bundle bundle = new Bundle();
 		ExifInterface exif;
				try {
					exif = new ExifInterface(currentFile);
					exifAttribute = getExif(exif);
					geoDegree = new GeoDegree(exif);
					bundle.putInt("Longitude", geoDegree.getLongitudeE6());
			 		bundle.putInt("Latitude", geoDegree.getLatitudeE6());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 		intent.putExtras(bundle);
 		startActivity(intent);
 	}
 }