package com.example.gpstracking;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener {

	private final Context mContext;

	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;

	Location preLocation;
	Location curLocation;
	Location location;
	double latitude;
	double longitude;
	String provider;

	private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //10 meters

	private static final long MIN_TIME_BW_UPDATES = 1000 * 20; // 20 seconds

	private static final long TIME_DELTA_CRITERIA = 1000 * 60 * 2;

	protected LocationManager locationManager;

	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation();
	}

	public boolean isBetterLocation(Location newLocation,Location curBestLocation) {
		if (curBestLocation == null) {
			return true;
		}

		long timeDelta = newLocation.getTime() - curBestLocation.getTime();
		boolean isMuchNewer = timeDelta > TIME_DELTA_CRITERIA;
		boolean isMuchOlder = timeDelta < -TIME_DELTA_CRITERIA;

		boolean isNewer = timeDelta > 0;

		if (isMuchNewer) {
			return true;
		} else if (isMuchOlder) {
			return false;
		}

		int accuDelta = (int) (newLocation.getAccuracy() - curBestLocation.getAccuracy());
		boolean isMoreAccurate = accuDelta < 0;
		boolean isMuchAccurate = accuDelta < -200;

		boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), curBestLocation.getProvider());

		if(isMoreAccurate) {
			return true;
		} else if (isNewer && isMoreAccurate) {
			return true;
		} else if (isNewer && isMuchAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	public Location getLocation() {
		try {
			locationManager = (LocationManager) mContext
					.getSystemService(LOCATION_SERVICE);

			isGPSEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

			isNetworkEnabled = locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled /*&& !isNetworkEnabled*/) {
			} else {
				preLocation = null;
				this.canGetLocation = true;
				if (isGPSEnabled) {
					if (locationManager != null) {
						preLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					}
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("GPS", "GPS");
					if(locationManager != null) {
						curLocation = locationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if(isBetterLocation(curLocation, preLocation)) {
							location = curLocation;
						} else {
							location = preLocation;
						}
					}

					if(!isNetworkEnabled) {
					} else {
						locationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("Network", "Network");
						if (locationManager != null) {
							curLocation = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
							if (isBetterLocation(curLocation, location)) {
								location = curLocation;
							}
						}
					}// end of network in gps
				}//end of gps enabled
				else if (isNetworkEnabled) {//if only network enabled 
					if(locationManager != null) {
						preLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					}
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network");
					if(locationManager != null) {
						curLocation = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if(isBetterLocation(curLocation, preLocation)) {
							location = curLocation;
						} else {
							location = preLocation;
						}
					}
				} //end of only network enabled
			}// end of get location
			if (location != null) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	public void stopUsingGPS() {
		if(locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}		
	}

	public double getLatitude() {
		if(location != null) {
			latitude = location.getLatitude();
		}
		return latitude;
	}

	public double getLongitude() {
		if(location != null) {
			longitude = location.getLongitude();
		}
		return longitude;
	}

	public String getProvider() {
		if(location != null) {
			provider = location.getProvider();
		}
		return provider;
	}

	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	/**
	 * Function to show settings alert dialog
	 * On pressing Settings button will lauch Settings Options
	 */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		// Setting Dialog Title
		alertDialog.setTitle("GPS settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		location = getLocation();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
