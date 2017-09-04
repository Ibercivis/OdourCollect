package ibercivis.com.odourcollectapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainFragment extends Fragment implements LocationListener {
    // UI
    private TextView filterListTextView;

    // Map
    private MapView myOpenMapView;
    private IMapController myMapController;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GeoPoint currentLocation;

    // API response
    private JSONArray reportsArray;
    private JSONArray currentReportsArray;

    // Filters
    private String filterTypeChosen = null;
    private Date filterDateSince = null;
    private Date filterDateUntil = null;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout ll = (RelativeLayout) inflater.inflate(R.layout.fragment_main, container, false);

        // Set the map
        try {
            setMap(ll);
        } catch (SecurityException e) {
            // Invalid permissions
            Toast toast = Toast.makeText(getContext(), "Invalid permissions", Toast.LENGTH_SHORT);
            toast.show();
            return ll;
        }

        // Set the markers
        getMarkers();

        // UI items
        filterListTextView = (TextView) ll.findViewById(R.id.filter_list);
        filterListTextView.setText("");

        // Inflate the layout for this fragment
        return ll;
    }

    public void onLocationChanged(Location location) {
        GeoPoint newLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (currentLocation != newLocation) {
            myMapController.setCenter(newLocation);
            currentLocation = newLocation;
        }
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public JSONArray getReportsArray () {
        return currentReportsArray;
    }

    public void filterType(String type) {
        filterTypeChosen = type;
        refreshOverlay();
    }

    public void filterDateSince(String since) {
        try {
            filterDateSince = dateFormat.parse(since);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (filterDateUntil != null){
            if (filterDateSince.compareTo(filterDateUntil) < 0) {
                refreshOverlay();
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
            refreshOverlay();
        }
    }

    public void filterDateUntil(String until) {
        try {
            filterDateUntil = dateFormat.parse(until);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (filterDateSince != null){
            if (filterDateSince.compareTo(filterDateUntil) < 0) {
                refreshOverlay();
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
            refreshOverlay();
        }
    }

    public void removeFilters() {
        filterTypeChosen = null;
        filterDateSince = null;
        filterDateUntil = null;

        filterListTextView.setText("");

        refreshOverlay();
    }

    public void refreshOverlay() {
        currentReportsArray = new JSONArray();

        /* Marker cluster overlay */
        RadiusMarkerClusterer markerCluster= new RadiusMarkerClusterer(getActivity());
        Drawable clusterIconDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.marker_cluster);
        Bitmap clusterIcon = ((BitmapDrawable) clusterIconDrawable).getBitmap();
        markerCluster.setIcon(clusterIcon);
        myOpenMapView.getOverlays().clear();
        myOpenMapView.invalidate();
        myOpenMapView.getOverlays().add(markerCluster);

        /* Go through the response reading the whole records and creating the overlay items */
        for (int i=0; i<reportsArray.length(); i++) {
            JSONObject record;

            try {
                record = reportsArray.getJSONObject(i);
                Marker marker = createMarker(record);
                markerCluster.add(marker);
                currentReportsArray.put(record);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Marker createMarker(JSONObject record) throws JSONException, ParseException, ClassCastException {
        Marker marker = null;

        if ( ! isValidRecord(record) ) {
            throw new JSONException("Invalid record");
        }

        final int reportId = record.getInt("report_id");


        /* Create info to display in the pop up */
        String recordString = "User: " + record.get("username").toString()
                + "\nType: " + record.get("type").toString()
                + "\nDate: " + record.get("report_date").toString()
                + "\nIntensity (1 - 6): " + record.get("intensity").toString()
                + "\nAnnoyance (-4 - 4): " + record.get("annoyance").toString()
                + "\nNumber of comments: " + record.get("number_comments").toString();

        //System.out.println(recordString);

        marker = new Marker(myOpenMapView);
        GeoPoint geoPoint = new GeoPoint(
                new Double(record.get("latitude").toString()),
                new Double(record.get("longitude").toString())
        );

        /* Set the proper icon according to the annoyance level */
        int level = Integer.parseInt(record.get("annoyance").toString());
        Drawable markerIcon = getMarkerIcon(level);

        marker.setPosition(geoPoint);
        marker.setTitle("");
        marker.setSnippet(recordString);
        marker.setIcon(markerIcon);

        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                showMarkerDialog(marker, reportId);
                return true;
            }
        });

        return marker;
    }

    private boolean isValidRecord(JSONObject record) throws JSONException, ParseException {
        Date reportDate = dateFormat.parse(record.get("report_date").toString());

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
        if (((filterTypeChosen == null) && (filterDateSince == null) && (filterDateUntil == null))
                || ((filterTypeChosen == null) && (filterDateSince == null) && (filterDateUntil != null) && (filterDateUntil.compareTo(reportDate) > 0))
                || ((filterTypeChosen == null) && (filterDateSince != null) && (filterDateUntil == null) && (filterDateSince.compareTo(reportDate) < 0))
                || ((filterTypeChosen == null) && (filterDateSince != null) && (filterDateUntil != null) && (filterDateSince.compareTo(reportDate) < 0)
                && (filterDateUntil.compareTo(reportDate) > 0))
                || ((filterTypeChosen != null) && (filterDateSince == null) && (filterDateUntil == null) && (filterTypeChosen.equals(record.get("type").toString())))
                || ((filterTypeChosen != null) && (filterDateSince == null) && (filterDateUntil != null) && (filterTypeChosen.equals(record.get("type").toString()))
                && (filterDateUntil.compareTo(reportDate) > 0))
                || ((filterTypeChosen != null) && (filterDateSince != null) && (filterDateUntil == null) && (filterTypeChosen.equals(record.get("type").toString()))
                && (filterDateSince.compareTo(reportDate) < 0))
                || ((filterTypeChosen != null) && (filterDateSince != null) && (filterDateUntil != null) && (filterTypeChosen.equals(record.get("type").toString()))
                && (filterDateSince.compareTo(reportDate) < 0) && (filterDateUntil.compareTo(reportDate) > 0))) {
            return true;
        } else {
            return false;
        }
    }

    private  Drawable getMarkerIcon(int level) {
        if (level > 2) {
            return ContextCompat.getDrawable(getContext(), R.drawable.marker_bad);
        } else if (level < -2) {
            return ContextCompat.getDrawable(getContext(), R.drawable.marker_good);
        } else {
            return ContextCompat.getDrawable(getContext(), R.drawable.marker_med);
        }
    }

    private void showMarkerDialog(Marker marker, final int reportId) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(marker.getTitle());
        dialog.setMessage(marker.getSnippet());
        dialog.setPositiveButton(R.string.view_details, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(getActivity(), DisplayReportActivity.class);
                intent.putExtra("report_id", reportId);
                startActivity(intent);
            }
        });

        dialog.show();
    }

    private void setMap(RelativeLayout ll) {
        // important! set your user agent to prevent getting banned from the osm servers
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);

        myOpenMapView = (MapView) ll.findViewById(R.id.openmapview);

        myOpenMapView.setTileSource(TileSourceFactory.MAPNIK);

        // Add default zoom buttons and ability to zoom with 2 fingers (multi-touch)
        myOpenMapView.setBuiltInZoomControls(true);
        myOpenMapView.setMultiTouchControls(true);

        myOpenMapView.setMinZoomLevel(3);
        myOpenMapView.setMaxZoomLevel(19);

        myMapController = myOpenMapView.getController();
        myMapController.setZoom(15);

        // Get the location manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        // TODO: getcontext warning API level
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            throw new SecurityException();
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
    }

    private void getMarkers() {
        String url = getString(R.string.base_url) + "/getreports.php";

        final JsonArrayRequest jsonAllRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        reportsArray = response;
                        refreshOverlay();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(getContext()).add(jsonAllRequest);
    }
}