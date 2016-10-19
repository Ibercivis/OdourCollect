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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    private JSONArray reportsArray;
    private JSONArray currentReportsArray;
    private String filterTypeChosen = null;
    private Date filterDateSince = null;
    private Date filterDateUntil = null;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private TextView filterListTextView;

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

        RelativeLayout ll = (RelativeLayout) inflater.inflate(R.layout.fragment_main, container, false);
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
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if( location != null ) {
            currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            myMapController.setCenter(currentLocation);
        }
        else {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if( location != null ) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                myMapController.setCenter(currentLocation);
            }
            else {
                GeoPoint startPoint = new GeoPoint(50.936255, 6.957779);
                myMapController.setCenter(startPoint);
            }
        }

        populateOverlay();

        filterListTextView = (TextView) ll.findViewById(R.id.filter_list);
        filterListTextView.setText("");

        // Inflate the layout for this fragment
        return ll;
    }

    public void onLocationChanged(Location location) {
        /*GeoPoint newLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (currentLocation != newLocation) {
                myMapController.setCenter(newLocation);
        }*/
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    protected void populateOverlay () {

        String url = "http://modulos.ibercivis.es/webservice/getreports.php";

        final JsonArrayRequest jsonRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // the response is already constructed as a JSONObject!
                        String responseString = response.toString();
                        System.out.println(responseString);

                        reportsArray = response;

                        filterOverlay();

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        System.out.println("jsonrequest: "+jsonRequest.toString());
        Volley.newRequestQueue(getContext()).add(jsonRequest);
    }

    void filterType (String type) {

        // Set the type chosen by the user
        filterTypeChosen = type;

        // Refresh the map recreating the overlay with the type chosen
        filterOverlay();
    }

    void filterDateSince (String since) {
/* Date aux = null;
        // Set the type chosen by the user
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            filterDateSince = format.parse(since);
aux = format.parse("2016-09-11 19:37:29");
            System.out.println(filterDateSince);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

       if (filterDateSince.compareTo(aux)<0)
        {
            System.out.println("aux is Greater than my filterDateSince");
        }
        else {
            System.out.println("filterDateSince is Greater than my aux");
        }
*/

        // Set the since date chosen by the user
        try {
            filterDateSince = format.parse(since);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (filterDateUntil != null){
            if (filterDateSince.compareTo(filterDateUntil) < 0) {
                // Refresh the map recreating the overlay with the type chosen
                filterOverlay();
            }
            else {
                int duration = Toast.LENGTH_LONG;
                Toast toast;
                CharSequence text;

                // Until date before than since date, show error
                text = "Since date is greater than until date! Filter not applied.";
                toast = Toast.makeText(getContext(), text, duration);
                toast.show();
            }
        }
        else {
            // Refresh the map recreating the overlay with the type chosen
            filterOverlay();
        }

    }

    void filterDateUntil (String until) {

        // Set the since date chosen by the user
        try {
            filterDateUntil = format.parse(until);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (filterDateSince != null){
            if (filterDateSince.compareTo(filterDateUntil) < 0) {
                // Refresh the map recreating the overlay with the type chosen
                filterOverlay();
            }
            else {
                int duration = Toast.LENGTH_LONG;
                Toast toast;
                CharSequence text;

                // Until date before than since date, show error
                text = "Since date is greater than until date! Filter not applied.";
                toast = Toast.makeText(getContext(), text, duration);
                toast.show();
            }
        }
        else {
            // Refresh the map recreating the overlay with the type chosen
            filterOverlay();
        }
    }

    void removeFilters () {

        filterTypeChosen = null;
        filterDateSince = null;
        filterDateUntil = null;

        filterListTextView.setText("");

        // Refresh the map recreating the overlay with the type chosen
        filterOverlay();
    }

    void filterOverlay () {

        anotherOverlayItemArray = new ArrayList<OverlayItem>();
System.out.println("creating currentreportsarray");
        // Update the current reports array
        currentReportsArray = new JSONArray();

        /* Go through the response reading the whole records and creating the overlay items */
        for(int i=0; i<reportsArray.length(); i++){
            OverlayItem myLocationOverlayItem = null;
            JSONObject odourRecord = null;

            try {
                                /* Get the JSON object from the JSON array */
                odourRecord = reportsArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                Date reportDate = format.parse(odourRecord.get("report_date").toString());

                // This if sentence check all the possibilities for the three filter options ordered like this:
                // Every filter can take two values (0, 1) considering that the filter has been set or not for type, since and until
                // (Just a help for the developer to understand the following condition)
                //      000 -> No filter
                //      001 -> Filter by until
                //      010 -> Filter by since
                //      011 -> Filter by since and until
                //      100 -> Filter only by type
                //      101 -> Filter by type and until
                //      110 -> Filter by type and since
                //      111 -> Filter by type, since and until
                if (((filterTypeChosen == null)&&(filterDateSince == null)&&(filterDateUntil == null))
                        || ((filterTypeChosen == null)&&(filterDateSince == null)&&(filterDateUntil != null)&&(filterDateUntil.compareTo(reportDate)>0))
                        || ((filterTypeChosen == null)&&(filterDateSince != null)&&(filterDateUntil == null)&&(filterDateSince.compareTo(reportDate)<0))
                        || ((filterTypeChosen == null)&&(filterDateSince != null)&&(filterDateUntil != null)&&(filterDateSince.compareTo(reportDate)<0)
                        &&(filterDateUntil.compareTo(reportDate)>0))
                        || ((filterTypeChosen != null)&&(filterDateSince == null)&&(filterDateUntil == null)&&(filterTypeChosen.equals(odourRecord.get("type").toString())))
                        || ((filterTypeChosen != null)&&(filterDateSince == null)&&(filterDateUntil != null)&&(filterTypeChosen.equals(odourRecord.get("type").toString()))
                        &&(filterDateUntil.compareTo(reportDate)>0))
                        || ((filterTypeChosen != null)&&(filterDateSince != null)&&(filterDateUntil == null)&&(filterTypeChosen.equals(odourRecord.get("type").toString()))
                        &&(filterDateSince.compareTo(reportDate)<0))
                        || ((filterTypeChosen != null)&&(filterDateSince != null)&&(filterDateUntil != null)&&(filterTypeChosen.equals(odourRecord.get("type").toString()))
                        &&(filterDateSince.compareTo(reportDate)<0)&&(filterDateUntil.compareTo(reportDate)>0))) {

                    /* Create info to display in the pop up */
                    String odourRecordString = "User: " + odourRecord.get("user").toString()
                            + "\nType: " + odourRecord.get("type").toString()
                            + "\nDate: " + odourRecord.get("report_date").toString()
                            + "\nIntensity (1 - 6): " + odourRecord.get("intensity").toString()
                            + "\nAnnoyance (-4 - 4): " + odourRecord.get("annoyance").toString()
                            + "\nNumber of comments: " + odourRecord.get("number_comments").toString();

                    System.out.println(odourRecordString);

                    /* Create the overlay item */
                    myLocationOverlayItem = new OverlayItem("",
                            odourRecordString,
                            new GeoPoint(new Double(odourRecord.get("latitude").toString()),
                                    new Double(odourRecord.get("longitude").toString())));

                    /* Set the proper icon according to the annoyance level */
                    Drawable myCurrentLocationMarker = null;
                    if (Integer.parseInt(odourRecord.get("annoyance").toString()) > 2) {
                        myCurrentLocationMarker = ContextCompat.getDrawable(this.getContext(), R.drawable.marker_bad);
                    } else if (Integer.parseInt(odourRecord.get("annoyance").toString()) < -2) {
                        myCurrentLocationMarker = ContextCompat.getDrawable(this.getContext(), R.drawable.marker_good);
                    } else {
                        myCurrentLocationMarker = ContextCompat.getDrawable(this.getContext(), R.drawable.marker_med);
                    }

                    myLocationOverlayItem.setMarker(myCurrentLocationMarker);
                    anotherOverlayItemArray.add(myLocationOverlayItem);

                    currentReportsArray.put(odourRecord);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
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

        // Create the new overlay with the proper items
        MyOwnItemizedOverlay overlay = new MyOwnItemizedOverlay(getContext(), anotherOverlayItemArray);

        // Clear and invalidate the former overlays
        myOpenMapView.getOverlays().clear();
        myOpenMapView.invalidate();

        // Add the new overlay
        myOpenMapView.getOverlays().add(overlay);

        // Update text view summarizing current filters

        // Check the filters and build the string up
        String currentFiltersString = "";
        if (filterTypeChosen != null){
            currentFiltersString += "Type: "+filterTypeChosen+"\n";
        }
        if (filterDateSince != null){
            currentFiltersString += "Since: "+filterDateSince.toString()+"\n";
        }
        if (filterDateUntil != null){
            currentFiltersString += "Until: "+filterDateUntil.toString();
        }

        // Update the text view
        if (filterTypeChosen!= null || filterDateSince != null || filterDateUntil != null)
            filterListTextView.setText("Current filters\n"+currentFiltersString);
    }

    JSONArray getReportsArray () {

        return currentReportsArray;
    }
}