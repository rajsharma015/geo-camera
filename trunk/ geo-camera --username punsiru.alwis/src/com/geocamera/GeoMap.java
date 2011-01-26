package com.geocamera;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * 
 * @author Punsiru Alwis
 *
 */
public class GeoMap extends MapActivity {
	
	private CheckBox mySatellite;
	private MapView myMapView;
	private MapController myMapController;
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.mapview);
		
		// load the layout
		myMapView = (MapView)findViewById(R.id.mapview);
		myMapController = myMapView.getController();
		myMapView.setBuiltInZoomControls(true);
		mySatellite = (CheckBox)findViewById(R.id.satellite);
		mySatellite.setOnClickListener(mySatelliteOnClickListener);
		// set satellite view
		SetSatellite();
		
		Bundle bundle = this.getIntent().getExtras();
		// get Lat Long values
		int intLatitude = bundle.getInt("Latitude");
		int intLongitude = bundle.getInt("Longitude");
		// create geo point
		GeoPoint initGeoPoint = new GeoPoint(intLatitude, intLongitude);
		myMapController.animateTo(initGeoPoint);
		
		// set an icon
		//Drawable marker=getResources().getDrawable(android.R.drawable.ic_menu_myplaces);
		Drawable marker=getResources().getDrawable(R.drawable.bluedot);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		myMapView.getOverlays().add(new InterestingLocations(marker, intLatitude, intLongitude));
	}

	/**
	 * initiate checkbox listener
	 */
	private CheckBox.OnClickListener mySatelliteOnClickListener
		= new CheckBox.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SetSatellite();
			}
		
	};
	
	 private void SetSatellite()
	 {
		// set satellite view
		 myMapView.setSatellite(mySatellite.isChecked());
	 };
	
	 class InterestingLocations extends ItemizedOverlay<OverlayItem>{

		 private List<OverlayItem> locations = new ArrayList<OverlayItem>();
		 private Drawable marker;
		 
		  public InterestingLocations(Drawable defaultMarker,
				  int LatitudeE6, int LongitudeE6) {
			  super(defaultMarker);
			  this.marker=defaultMarker;
			  GeoPoint myPlace = new GeoPoint(LatitudeE6,LongitudeE6);
			  locations.add(new OverlayItem(myPlace , "My Place", "My Place"));
			  populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			// TODO Auto-generated method stub
			return locations.get(i);
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return locations.size();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			// TODO Auto-generated method stub
			super.draw(canvas, mapView, shadow);
			boundCenterBottom(marker);
		}
		 
		
	 }
}
