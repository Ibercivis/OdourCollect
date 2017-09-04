package ibercivis.com.odourcollectapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class AddReportActivity extends AppCompatActivity {

    TextView addreport_origin_textview;
    Spinner type_spinner;
    Spinner duration_spinner;

    String error_check;

    int selected, type_spinner_item, duration_spinner_item;

    // Intensity seek bar variables
    SeekBar sb_intensity;
    int sb_intensity_value = 1;
    float sb_intensity_start = 1;
    float sb_intensity_end = 6;
    int sb_intensity_start_position = 1;
    TextView intensity_result_value_textview;

    SeekBar sb_annoyance;
    int sb_annoyance_value = -4;
    float sb_annoyance_start = -4;
    float sb_annoyance_end = 4;
    int sb_annoyance_start_position = -4;
    TextView annoyance_result_value_textview;

    SeekBar sb_cloud;
    int sb_cloud_value = 1;
    float sb_cloud_start = 1;
    float sb_cloud_end = 8;
    int sb_cloud_start_position = 1;
    TextView cloud_result_value_textview;

    SeekBar sb_rain;
    int sb_rain_value = 0;
    float sb_rain_start = 0;
    float sb_rain_end = 5;
    int sb_rain_start_position = 0;
    TextView rain_result_value_textview;

    SeekBar sb_wind;
    int sb_wind_value = 0;
    float sb_wind_start = 0;
    float sb_wind_end = 5;
    int sb_wind_start_position = 0;
    TextView wind_result_value_textview;

    EditText type_other_edit_text;

    MyLocationListener locationListener;
    LocationManager lm;

// Center sliders and check fields to be sent in the POST request
// Move initializations to separate methods: initSpinners, initSliders
// Add hidden text field for option "Other"


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addreport);

        locationListener = new MyLocationListener();

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        type_other_edit_text = (EditText) findViewById(R.id.addreport_type_other);
        type_other_edit_text.setVisibility(View.GONE);

        type_spinner = (Spinner) findViewById(R.id.type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> type_adapter = ArrayAdapter.createFromResource(this,
                R.array.type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        type_spinner.setAdapter(type_adapter);
        //  Make spinner "Select Type" item not selectable by overriding listener
        type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                selected = type_spinner.getSelectedItemPosition();
                if (selected != 0) {
                    type_spinner_item = selected;

                    if (type_spinner.getItemAtPosition(selected).toString().equals("Other")){
                        type_other_edit_text.setVisibility(View.VISIBLE);
                    }
                    else type_other_edit_text.setVisibility(View.GONE);
                }
                System.out.println(selected);

                setIdType();
            }

            private void setIdType() {
                type_spinner.setSelection(type_spinner_item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        duration_spinner = (Spinner) findViewById(R.id.duration_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> duration_adapter = ArrayAdapter.createFromResource(this,
                R.array.duration_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        duration_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        duration_spinner.setAdapter(duration_adapter);
        //  Make spinner "Select Duration" item not selectable by overriding listener
        duration_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                selected = duration_spinner.getSelectedItemPosition();
                if (selected != 0)
                    duration_spinner_item = selected;
                System.out.println(selected);

                setIdDuration();
            }

            private void setIdDuration() {
                duration_spinner.setSelection(duration_spinner_item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        // Set intensity slider
        intensity_result_value_textview = (TextView) findViewById(R.id.intensityResult);
        intensity_result_value_textview.setText(String.valueOf(sb_intensity_value));

        sb_intensity = (SeekBar) findViewById(R.id.sbBar_intensity);
        sb_intensity.setProgress(sb_intensity_start_position);
        sb_intensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                float temp = seekBar.getProgress();
                float dis = sb_intensity_end - sb_intensity_start;
                sb_intensity_value = (int) (sb_intensity_start + ((temp / 100) * dis));

                intensity_result_value_textview.setText(String.valueOf(sb_intensity_value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub

                // To convert it as discrete value
                float temp = progress;
                float dis = sb_intensity_end - sb_intensity_start;
                sb_intensity_value = (int) (sb_intensity_start + ((temp / 100) * dis));

                intensity_result_value_textview.setText(String.valueOf(sb_intensity_value));
            }
        });

        // Set annoyance slider
        annoyance_result_value_textview = (TextView) findViewById(R.id.annoyanceResult);
        annoyance_result_value_textview.setText(String.valueOf(sb_annoyance_value));

        sb_annoyance = (SeekBar) findViewById(R.id.sbBar_annoyance);
        sb_annoyance.setProgress(sb_annoyance_start_position);
        sb_annoyance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                float temp = seekBar.getProgress();
                float dis = sb_annoyance_end - sb_annoyance_start;
                sb_annoyance_value = (int) (sb_annoyance_start + ((temp / 100) * dis));

                annoyance_result_value_textview.setText(String.valueOf(sb_annoyance_value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub

                // To convert it as discrete value
                float temp = progress;
                float dis = sb_annoyance_end - sb_annoyance_start;
                sb_annoyance_value = (int) (sb_annoyance_start + ((temp / 100) * dis));

                annoyance_result_value_textview.setText(String.valueOf(sb_annoyance_value));
            }
        });

        // Set cloud slider
        cloud_result_value_textview = (TextView) findViewById(R.id.cloudResult);
        cloud_result_value_textview.setText(String.valueOf(sb_cloud_value));

        sb_cloud = (SeekBar) findViewById(R.id.sbBar_cloud);
        sb_cloud.setProgress(sb_cloud_start_position);
        sb_cloud.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                float temp = seekBar.getProgress();
                float dis = sb_cloud_end - sb_cloud_start;
                sb_cloud_value = (int) (sb_cloud_start + ((temp / 100) * dis));

                cloud_result_value_textview.setText(String.valueOf(sb_cloud_value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub

                // To convert it as discrete value
                float temp = progress;
                float dis = sb_cloud_end - sb_cloud_start;
                sb_cloud_value = (int) (sb_cloud_start + ((temp / 100) * dis));

                cloud_result_value_textview.setText(String.valueOf(sb_cloud_value));
            }
        });

        // Set rain slider
        rain_result_value_textview = (TextView) findViewById(R.id.rainResult);
        rain_result_value_textview.setText(String.valueOf(sb_rain_value));

        sb_rain = (SeekBar) findViewById(R.id.sbBar_rain);
        sb_rain.setProgress(sb_rain_start_position);
        sb_rain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                float temp = seekBar.getProgress();
                float dis = sb_rain_end - sb_rain_start;
                sb_rain_value = (int) (sb_rain_start + ((temp / 100) * dis));

                rain_result_value_textview.setText(String.valueOf(sb_rain_value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub

                // To convert it as discrete value
                float temp = progress;
                float dis = sb_rain_end - sb_rain_start;
                sb_rain_value = (int) (sb_rain_start + ((temp / 100) * dis));

                rain_result_value_textview.setText(String.valueOf(sb_rain_value));
            }
        });

        // Set wind slider
        wind_result_value_textview = (TextView) findViewById(R.id.windResult);
        wind_result_value_textview.setText(String.valueOf(sb_wind_value));

        sb_wind = (SeekBar) findViewById(R.id.sbBar_wind);
        sb_wind.setProgress(sb_wind_start_position);
        sb_wind.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                float temp = seekBar.getProgress();
                float dis = sb_wind_end - sb_wind_start;
                sb_wind_value = (int) (sb_wind_start + ((temp / 100) * dis));

                wind_result_value_textview.setText(String.valueOf(sb_wind_value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub

                // To convert it as discrete value
                float temp = progress;
                float dis = sb_wind_end - sb_wind_start;
                sb_wind_value = (int) (sb_wind_start + ((temp / 100) * dis));

                wind_result_value_textview.setText(String.valueOf(sb_wind_value));
            }
        });

    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    public void addReportRequest(View view) {

        addreport_origin_textview = (TextView) findViewById(R.id.addreport_origin);

        if (checkInputAddReport()) {
            // Input data ok, so go with the request

            // Url for the webservice
            String url = getString(R.string.base_url) + "/addreport.php";

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        System.out.println(response.toString());

                        JSONObject responseJSON = new JSONObject(response);

                        if ((int) responseJSON.get("result") == 1) {

                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();

                        } else {
                            showError("Error while adding report: " + responseJSON.get("message") + ".");

                            // Clean the text fields for new entries
                            addreport_origin_textview.setText("");
                            type_other_edit_text.setText("");

                            // Clean spinners
                            type_spinner_item = 0;
                            type_spinner.setSelection(type_spinner_item);
                            duration_spinner_item = 0;
                            duration_spinner.setSelection(duration_spinner_item);

                            // Clean sliders (seek bars)
                            sb_intensity_value = 1;
                            sb_intensity.setProgress(1);
                            sb_annoyance_value = -4;
                            sb_annoyance.setProgress(1);
                            sb_cloud_value = 1;
                            sb_cloud.setProgress(1);
                            sb_rain_value = 0;
                            sb_rain.setProgress(1);
                            sb_wind_value = 0;
                            sb_wind.setProgress(1);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    SessionManager session = new SessionManager(getApplicationContext());

                    Map<String, String> addreport_params = new HashMap<String, String>();
                    addreport_params.put("origin", addreport_origin_textview.getText().toString());
                    addreport_params.put("type", type_spinner.getSelectedItem().toString());
                    addreport_params.put("duration", duration_spinner.getSelectedItem().toString());
                    addreport_params.put("intensity", String.valueOf(sb_intensity_value));
                    addreport_params.put("annoyance", String.valueOf(sb_annoyance_value));
                    addreport_params.put("cloud", String.valueOf(sb_cloud_value));
                    addreport_params.put("rain", String.valueOf(sb_rain_value));
                    addreport_params.put("wind", String.valueOf(sb_wind_value));
                    addreport_params.put("other_type", type_other_edit_text.getText().toString());
                    addreport_params.put("user", session.getUsername());

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    addreport_params.put("datetime", dateFormatter.format(new Date()));

                    // Get the location manager
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                        int duration = Toast.LENGTH_LONG;
                        Toast toast;

                        toast = Toast.makeText(getApplicationContext(), "Location permission must be granted to locate the report", duration);
                        toast.show();
                    }
                    Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if( location == null ) {
                        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if( location != null ) {
                            System.out.println(location);
                            addreport_params.put("latitude", String.valueOf(location.getLatitude()));
                            addreport_params.put("longitude", String.valueOf(location.getLongitude()));
                        }
                    }
                    else {
                        System.out.println(location);
                        addreport_params.put("latitude", String.valueOf(location.getLatitude()));
                        addreport_params.put("longitude", String.valueOf(location.getLongitude()));
                    }

/*                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if( location != null ) {
                        System.out.println(location);
                        addreport_params.put("latlng", "("+location.getLatitude()+", "+location.getLongitude()+")");
                    }
*/
                return addreport_params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("Content-Type","application/x-www-form-urlencoded");
                    return params;
                }
            };
            queue.add(sr);
        }
        else {
            // Do nothing, error has been shown in a toast and views clean
        }
    }

    private void showError (CharSequence error) {
        int duration = Toast.LENGTH_LONG;
        Toast toast;

        toast = Toast.makeText(getApplicationContext(), error, duration);
        toast.show();
    }

    private boolean checkSelect(Spinner spinner, String errorMessage) {

        if (spinner.getSelectedItemPosition() == 0) {
            error_check = error_check + "You must select one of the options from the " + errorMessage + " drop-down.\n";
            return false;
        }
        return true;
    }

    private boolean checkInputAddReport () {

        error_check = "";
        boolean valid = true;

        valid = checkSelect( type_spinner, "type" ) && valid;
        valid = checkSelect( duration_spinner, "duration" ) && valid;

        // Get the location manager
        /*LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);*/
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            int duration = Toast.LENGTH_LONG;
            Toast toast;

            toast = Toast.makeText(getApplicationContext(), "Location permission must be granted to locate the report", duration);
            toast.show();

            error_check = error_check + "Location permission must be granted to locate the report.\n";
            valid = false;
        }
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if( location == null ) {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if( location == null ) {
                error_check = error_check + "Location returns null, check location and navigation are enabled and permission granted.\n";
                valid = false;
            }
        }






/*        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if( location == null ) {
            error_check = error_check + "Location returns null, check location and navigation are enabled and permission granted.\n";
        }
        */
        if (!error_check.equals("")){
            showError(error_check);
        }

        return valid;
    }

    /** Finish Activity on Cancel Button Push */
    public void cancelAddReport(View view) {
        finish();
    }

}
