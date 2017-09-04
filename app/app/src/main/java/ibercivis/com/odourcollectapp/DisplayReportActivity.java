package ibercivis.com.odourcollectapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


// Populate dynamically the list of comments
// Update the request
// Populate the text view with the report once the request has been received
// Add comment button (and check for logging) on the bar to add comments



public class DisplayReportActivity extends AppCompatActivity {

    private int report_id;

    private ListView commentsListView;

    private JSONArray data;

    // Using this increment value we can move the listview items
    private int increment = 0;

    private JSONArray sort;

    TextView report_content_textView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_report);

        report_content_textView = (TextView)findViewById(R.id.report_content);

        Intent myIntent = getIntent(); // gets the previously created intent
        report_id = myIntent.getIntExtra("report_id", 0); // will return "report_id"

        populateReportActivity();
    }

    protected void populateReportActivity () {

        // Url for the webservice
        String url = getString(R.string.base_url) + "/getreport.php?report_id=" + report_id;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    System.out.println(response.toString());

                    JSONObject responseJSON = new JSONObject(response);

                    System.out.println("json response: "+responseJSON.toString());
                    System.out.println("json response annoyance: "+responseJSON.get("annoyance"));

                    if ((int) responseJSON.get("result") == 1) {

                        // Set the proper image for the report
                        ImageView imageView = (ImageView) findViewById(R.id.img_report);

                        Integer[] imageId = {
                                R.drawable.marker_bad,
                                R.drawable.marker_good,
                                R.drawable.marker_med

                        };

                        try {
                            if (Integer.parseInt(responseJSON.get("annoyance").toString()) > 2) {
                                imageView.setImageResource(imageId[0]);
                            } else if (Integer.parseInt(responseJSON.get("annoyance").toString()) < -2) {
                                imageView.setImageResource(imageId[1]);
                            }
                            else {
                                imageView.setImageResource(imageId[2]);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Set the text with the report within the text view
                        try {
                                /* Create info to display in the pop up */
                            String odourRecordString = "User: "+responseJSON.get("username").toString()
                                    +"\nType: "+responseJSON.get("type").toString()
                                    +"\nDate: "+responseJSON.get("report_date").toString()
                                    +"\nIntensity (1 - 6): "+responseJSON.get("intensity").toString()
                                    +"\nAnnoyance (-4 - 4): "+responseJSON.get("annoyance").toString()
                                    +"\nCloud (1 - 8): "+responseJSON.get("cloud").toString()
                                    +"\nRain (0 - 5): "+responseJSON.get("rain").toString()
                                    +"\nWind (0 - 5): "+responseJSON.get("wind").toString()
                                    +"\nDuration: "+responseJSON.get("duration").toString()
                                    +"\nOrigin: "+responseJSON.get("origin").toString()
                                    +"\nLatitude: "+responseJSON.get("latitude").toString()
                                    +"\nLongitude: "+responseJSON.get("longitude").toString();

                            report_content_textView.setText(odourRecordString);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Get the resources from the layout
                        commentsListView = (ListView)findViewById(R.id.report_comments_list);

                        String comments_string = responseJSON.getString("comments");
                        System.out.println("Comments: "+comments_string);

                        JSONArray comments_array = new JSONArray(comments_string);

                        ArrayList<String> commentsList= new ArrayList<String>();

                        for(int i=0; i<comments_array.length(); i++){
                            JSONObject aux = new JSONObject(comments_array.get(i).toString());
                            commentsList.add(aux.get("comment")+"\n"+aux.get("username")+", "+aux.get("comment_date"));
                        }

                        // Create The Adapter with passing ArrayList as 3rd parameter
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, commentsList);
                        // Set The Adapter
                        commentsListView.setAdapter(arrayAdapter);

                    } else {
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast;
                        CharSequence text;

                        text = "There was an error trying to retrieve report information.";
                        toast = Toast.makeText(getApplicationContext(), text, duration);
                        toast.show();

                        finish();
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
        });
        queue.add(sr);
    }

    /** Open Login Activity */
    public void openLoginDisplayReport(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /** Cancel Display Report Activity */
    public void cancelDisplayReport(View view) {
        finish();
    }

    /** Open Comment Activity */
    public void addCommentActivity(View view) {

        SessionManager session = new SessionManager(this);
        if(session.isLoggedIn())
        {
            // User logged in, launch add activity
            Intent intent = new Intent(this, AddCommentActivity.class);
            intent.putExtra("report_id", report_id);
            startActivity(intent);
        }
        else
        {
            int duration = Toast.LENGTH_SHORT;
            Toast toast;
            CharSequence text;

            // User not logged in, launch login activity
            text = "You have to be logged in to add or comment reports!";
            toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();

            openLoginDisplayReport(this.findViewById(android.R.id.content));
        }
    }

    /** Open Comment Activity */
    public void addCFAActivity(View view) {

        SessionManager session = new SessionManager(this);
        if(session.isLoggedIn())
        {
            // User logged in, launch add activity
            Intent intent = new Intent(this, AddCFAActivity.class);
            intent.putExtra("report_id", report_id);
            startActivity(intent);
        }
        else
        {
            int duration = Toast.LENGTH_SHORT;
            Toast toast;
            CharSequence text;

            // User not logged in, launch login activity
            text = "You have to be logged in to call for action!";
            toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();

            openLoginDisplayReport(this.findViewById(android.R.id.content));
        }
    }

}
