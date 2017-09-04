package ibercivis.com.odourcollectapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;


public class DisplayReportsActivity extends AppCompatActivity {

    /* List View, pagination example: https://rakhi577.wordpress.com/2013/05/20/listview-pagination-ex-1/ */

    private static final long DELAY_NEARBY_REQ = 60 * 1000;  // max 1 request per minute

    private long lastCheck = 0; // timestamp for nearby request

    private ListView listView;
    private Button btn_prev;
    private Button btn_next;

    private JSONArray data;
    private JSONArray allData;
    private JSONArray nearbyData;

    private int pageCount ;

    // Using this increment value we can move the listview items
    private int increment = 0;

    /**
     * Here set the values, how the ListView to be display
     *
     * Be sure that you must set like this
     *
     * TOTAL_LIST_ITEMS > NUM_ITEMS_PAGE
     *
     * TOTAL_LIST_ITEMS is initialized with the response from the server during the first request
     */
    public int TOTAL_LIST_ITEMS = 5;
    public int NUM_ITEMS_PAGE = 5;

    private JSONArray sort;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_reports);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the resources from the layout
        listView = (ListView)findViewById(R.id.list);
        btn_prev = (Button)findViewById(R.id.prev);
        btn_next = (Button)findViewById(R.id.next);

        btn_prev.setEnabled(false);

        btn_next.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
            increment++;
            loadList(increment);
            checkEnable();
            }
        });

        btn_prev.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
            increment--;
            loadList(increment);
            checkEnable();
            }
        });

        Intent myIntent = getIntent(); // gets the previously created intent
        String reportsArrayString = myIntent.getStringExtra("reports_array"); // will return "report_id"

        try {
            allData = new JSONArray(reportsArrayString);
            data = new JSONArray(allData.toString());
            populateList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_display_reports_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_display_reports_all:
                increment = 0;
                try {
                    data = new JSONArray(allData.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                populateList();
                return true;
            case R.id.action_display_reports_nearby:
                increment = 0;
                getNearbyReports();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method for enabling and disabling Buttons
     */
    private void checkEnable() {
        if(increment+1 == pageCount) {
            btn_next.setEnabled(false);
        } else if(increment == 0) {
            btn_prev.setEnabled(false);
        } else {
            btn_prev.setEnabled(true);
            btn_next.setEnabled(true);
        }
    }

    /**
     * Method for loading data in listview
     * @param number
     */
    private void loadList(int number) {
        sort = new JSONArray();
        ArrayList<String> aux = new ArrayList<String>();

        int start = number * NUM_ITEMS_PAGE;
        for (int i=start; i<(start)+NUM_ITEMS_PAGE; i++)  {
            if (i<data.length()) {
                try {
                    sort.put(data.getJSONObject(i));
                    aux.add((String) data.getJSONObject(i).get("report_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }

        Integer[] imageId = {
            R.drawable.marker_bad,
            R.drawable.marker_good,
            R.drawable.marker_med
        };

        CustomList adapter = new  CustomList(DisplayReportsActivity.this, aux, sort, imageId);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
            Intent intent = new Intent(getApplicationContext(), DisplayReportActivity.class);
            try {
                intent.putExtra("report_id", Integer.parseInt((String) sort.getJSONObject(position).get("report_id")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            startActivity(intent);
            finish();
            }
        });

        checkEnable();
    }

    protected void populateList () {
        TOTAL_LIST_ITEMS = data.length();

        // count num pages
        int val = TOTAL_LIST_ITEMS%NUM_ITEMS_PAGE;
        val = val==0?0:1;
        pageCount = TOTAL_LIST_ITEMS/NUM_ITEMS_PAGE+val;

        loadList(0); // load page 1
    }

    private void getNearbyReports() {
        System.out.println("PRUEBA " + lastCheck + " - " + System.currentTimeMillis());

        if (nearbyData != null && System.currentTimeMillis() <= (lastCheck + DELAY_NEARBY_REQ)) {
            try {
                data = new JSONArray(nearbyData.toString());
                populateList();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            requestNearbyRequest();
            lastCheck = System.currentTimeMillis();
        }
    }

    private void requestNearbyRequest() {
        GeoPoint location = getLocation();

        String url = getString(R.string.base_url) + "/getreports.php";
        url += "?latitude=" + location.getLatitude() + "&longitude=" + location.getLongitude();

        final JsonArrayRequest jsonRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        nearbyData = response;
                        try {
                            data = new JSONArray(nearbyData.toString());
                            populateList();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(getApplicationContext()).add(jsonRequest);
    }

    private GeoPoint getLocation() {
        GeoPoint currentLocation = null;

        // Get the location manager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // TODO: getcontext warning API level
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        }
        else {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if( location != null ) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(), "Unable to access location", Toast.LENGTH_SHORT);
                toast.show();
                currentLocation = new GeoPoint(0.0, 0.0);
            }
        }

        return currentLocation;
    }
}
