package ibercivis.com.odourcollectapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

/* Open Street Maps

http://androcode.es/2012/06/osmdroid-introduccion-a-openstreetmap-en-android-osm-parte-i/

Check portrait, landscape, and so

 */


public class MainFragment extends Fragment implements LocationListener {

    private MapView myOpenMapView;
    private IMapController myMapController;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GeoPoint currentLocation;

    private ArrayList<OverlayItem> anotherOverlayItemArray;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.fragment_main, container, false);
        //important! set your user agent to prevent getting banned from the osm servers
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);

        myOpenMapView = (MapView) ll.findViewById(R.id.openmapview);

        final ITileSource tileSource = TileSourceFactory.MAPNIK;
        myOpenMapView.setTileSource(tileSource);

        // Add default zoom buttons and ability to zoom with 2 fingers (multi-touch)
        myOpenMapView.setBuiltInZoomControls(true);
        myOpenMapView.setMultiTouchControls(true);

        myMapController = myOpenMapView.getController();
        myMapController.setZoom(15);

        // Get the location manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return ll;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if( location != null ) {
            currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            myMapController.setCenter(currentLocation);
        }

        populateOverlay();

        // Inflate the layout for this fragment
        return ll;
    }

    public void onLocationChanged(Location location) {
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    protected void populateOverlay () {

        anotherOverlayItemArray = new ArrayList<OverlayItem>();

        String url = "http://modulos.ibercivis.es/webservice/getreports.php";

        final JsonArrayRequest jsonRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // the response is already constructed as a JSONObject!
                        String responseString = response.toString();
                        System.out.println(responseString);

                        /* Go through the response reading the whole records and creating the overlay items */
                        for(int i=0; i<response.length(); i++){
                            OverlayItem myLocationOverlayItem = null;
                            JSONObject odourRecord = null;

                            try {
                                /* Get the JSON object from the JSON array */
                                odourRecord = response.getJSONObject(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                /* Create info to display in the pop up */
                                String odourRecordString = "User: "+odourRecord.get("user").toString()
                                        +"\nType: "+odourRecord.get("type").toString()
                                        +"\nDate: "+odourRecord.get("report_date").toString()
                                        +"\nIntensity (1 - 6): "+odourRecord.get("intensity").toString()
                                        +"\nAnnoyance (-4 - 4): "+odourRecord.get("annoyance").toString()
                                        +"\nNumber of comments: "+odourRecord.get("number_comments").toString();

                                System.out.println(odourRecordString);


                                /* Create the overlay item */
                                myLocationOverlayItem = new OverlayItem("",
                                        odourRecordString,
                                        new GeoPoint(new Double(odourRecord.get("latitude").toString()),
                                                new Double (odourRecord.get("longitude").toString())));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            /* Set the proper icon according to the annoyance level */
                            Drawable myCurrentLocationMarker = null;
                            try {
                                if (Integer.parseInt(odourRecord.get("annoyance").toString()) > 2) {
                                       myCurrentLocationMarker = ContextCompat.getDrawable(getContext(), R.drawable.marker_bad);
                                   } else if (Integer.parseInt(odourRecord.get("annoyance").toString()) < -2) {
                                       myCurrentLocationMarker = ContextCompat.getDrawable(getContext(), R.drawable.marker_good);
                                   }
                                   else {
                                       myCurrentLocationMarker = getContext().getResources().getDrawable(R.drawable.marker_med);
                                   }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            myLocationOverlayItem.setMarker(myCurrentLocationMarker);
                            anotherOverlayItemArray.add(myLocationOverlayItem);
                        }

                        ItemizedIconOverlay.OnItemGestureListener<OverlayItem> myOnItemGestureListener = new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

                            @Override
                            public boolean onItemLongPress(int arg0, OverlayItem arg1) {
                                // TODO Auto-generated method stub
                                return false;
                            }

                            @Override
                            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                                return true;
                            }

                        };

        /*ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(getContext(), anotherOverlayItemArray, myOnItemGestureListener);
        anotherItemizedIconOverlay.setFocusItemsOnTap(true);
        myOpenMapView.getOverlays().add(anotherItemizedIconOverlay);*/

                        MyOwnItemizedOverlay overlay = new MyOwnItemizedOverlay(getContext(), anotherOverlayItemArray);
                        myOpenMapView.getOverlays().add(overlay);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(getContext()).add(jsonRequest);
    }
}