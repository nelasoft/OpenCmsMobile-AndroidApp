package cz.nelasoft.opencms.mobile;

import java.io.IOException;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ContactsFragment extends SherlockFragment {

	public static final String CONTACT_NAME = "contactName";
	public static final String CONTACT_STREET = "contactStreet";
	public static final String CONTACT_CITY = "contactCity";
	public static final String CONTACT_ZIP = "contactZip";
	public static final String CONTACT_PHONE = "contactPhone";
	public static final String CONTACT_FAX = "contactFax";
	public static final String CONTACT_EMAIL = "contactEmail";
	public static final String CONTACT_WWW = "contactWWW";
	public static final String CONTACT_MAP_TYPE = "contactMapType";
	public static final String CONTACT_MAP_ZOOM = "contactMapZoom";

	private static final String HTTPS = "https://";
	private static final String HTTP = "http://";

	private static View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Getting Google Play availability status
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity().getBaseContext());

		// Showing status
		if (status != ConnectionResult.SUCCESS) { // Google Play Services are
													// not available

			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this.getActivity(), requestCode);
			dialog.show();
			return null;
		}

		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}
		super.onCreateView(inflater, container, savedInstanceState);

		if (container == null)
			return null;

		Bundle bundle = getArguments();
		final String contactName = bundle.getString(CONTACT_NAME);
		final String contactStreet = bundle.getString(CONTACT_STREET);
		final String contactCity = bundle.getString(CONTACT_CITY);
		final String contactZip = bundle.getString(CONTACT_ZIP);
		final String contactPhone = bundle.getString(CONTACT_PHONE);
		final String contactFax = bundle.getString(CONTACT_FAX);
		final String contactEmail = bundle.getString(CONTACT_EMAIL);
		final String contactWWW = bundle.getString(CONTACT_WWW);
		final String contactMapType = bundle.getString(CONTACT_MAP_TYPE);
		final String contactMapZoom = bundle.getString(CONTACT_MAP_ZOOM);

		try {
			view = inflater.inflate(R.layout.activity_contacts, container, false);
		} catch (Exception e) {
			Log.e("", "cannot inflate", e);
		}

		if (!TextUtils.isEmpty(contactName)) {
			TextView twCompany = (TextView) view.findViewById(R.id.contactCompanyName);
			twCompany.setText(contactName);
		}

		StringBuilder address1 = new StringBuilder();

		if (!TextUtils.isEmpty(contactStreet)) {
			address1.append(contactStreet);
			address1.append(" ");
		}

		TextView twAddress1 = (TextView) view.findViewById(R.id.contactCompanyAddress1);
		twAddress1.setText(address1.toString());

		StringBuilder address2 = new StringBuilder();

		if (!TextUtils.isEmpty(contactZip)) {
			address2.append(contactZip);
			address2.append(" ");
		}

		if (!TextUtils.isEmpty(contactCity)) {
			address2.append(contactCity);
		}

		TextView twAddress2 = (TextView) view.findViewById(R.id.contactCompanyAddress2);
		twAddress2.setText(address2.toString());

		TextView twPhone = (TextView) view.findViewById(R.id.contactPhone);
		if (!TextUtils.isEmpty(contactPhone)) {
			twPhone.setText(contactPhone);
		} else {
			twPhone.setVisibility(View.INVISIBLE);
			view.findViewById(R.id.contactPhoneLabel).setVisibility(View.INVISIBLE);
		}

		TextView twFax = (TextView) view.findViewById(R.id.contactFax);
		if (!TextUtils.isEmpty(contactFax)) {
			twFax.setText(contactFax);
		} else {
			twFax.setVisibility(View.INVISIBLE);
			view.findViewById(R.id.contactFaxLabel).setVisibility(View.INVISIBLE);
		}

		Button emailButton = (Button) view.findViewById(R.id.contactImageEmail);
		if (!TextUtils.isEmpty(contactEmail)) {
			OnClickListener buttonEmailListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("plain/text");
					intent.putExtra(Intent.EXTRA_EMAIL, new String[] { contactEmail });
					startActivity(Intent.createChooser(intent, "Send email ..."));
				}
			};

			emailButton.setOnClickListener(buttonEmailListener);
			emailButton.setText("  " + contactEmail + " ");
		} else {
			emailButton.setVisibility(View.INVISIBLE);
		}

		Button wwwButton = (Button) view.findViewById(R.id.contactImageWWW);
		if (!TextUtils.isEmpty(contactWWW)) {
			// TextView twWWW = (TextView) view.findViewById(R.id.contactWWW);
			// twWWW.setText(contactWWW);

			OnClickListener buttonWWWListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String url;
					if (!contactWWW.startsWith(HTTP) && !contactWWW.startsWith(HTTPS)) {
						url = HTTP + contactWWW;
					} else {
						url = contactWWW;
					}
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(browserIntent);
				}
			};
			wwwButton.setText("  " + contactWWW + " ");
			wwwButton.setOnClickListener(buttonWWWListener);
		} else {
			wwwButton.setVisibility(View.INVISIBLE);
		}

		int mapType = GoogleMap.MAP_TYPE_NORMAL;
		if ("NORMAL".equalsIgnoreCase(contactMapType)) {
			mapType = GoogleMap.MAP_TYPE_NORMAL;
		}
		if ("SATELLITE".equalsIgnoreCase(contactMapType)) {
			mapType = GoogleMap.MAP_TYPE_SATELLITE;
		}
		if ("TERRAIN".equalsIgnoreCase(contactMapType)) {
			mapType = GoogleMap.MAP_TYPE_TERRAIN;
		}
		if ("HYBRID".equalsIgnoreCase(contactMapType)) {
			mapType = GoogleMap.MAP_TYPE_HYBRID;
		}

		int mapZoom = 16;
		try {
			mapZoom = Integer.parseInt(contactMapZoom);
		} catch (Exception ex) {
		}

		GoogleMap map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.contactMap)).getMap();

		map.setMapType(mapType);
		Geocoder geocoder = new Geocoder(getActivity().getBaseContext());

		try {
			List<Address> addrs = geocoder.getFromLocationName(address1.toString() + " " + address2.toString(), 1);

			Address addr = addrs.get(0);

			LatLng ll = new LatLng(addr.getLatitude(), addr.getLongitude());

			map.addMarker(new MarkerOptions().position(ll).title(contactName).icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon)).anchor(0.5f, 1));
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, mapZoom));

			// Zoom in, animating the camera.
			map.animateCamera(CameraUpdateFactory.zoomIn(), 100, null);

		} catch (IOException e) {
			Log.e("dd", "Cannot get address", e);
		}

		return view;
	}

	public SupportMapFragment getGoogleMap() {
		return null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// FragmentManager fm = getChildFragmentManager();
		// fragment = (SupportMapFragment) fm.findFragmentById(R.id.contactMap);
		// if (fragment == null) {
		// fragment = SupportMapFragment.newInstance();
		// fm.beginTransaction().replace(R.id.contactMap,
		// fragment).commit();
		// }
	}

}
