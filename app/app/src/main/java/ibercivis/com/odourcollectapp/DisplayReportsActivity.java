package ibercivis.com.odourcollectapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;


public class DisplayReportsActivity extends AppCompatActivity {

        /* List View, pagination example: https://rakhi577.wordpress.com/2013/05/20/listview-pagination-ex-1/ */

        private ListView listView;
        private Button btn_prev;
        private Button btn_next;

        private JSONArray data;

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

            // Get the resources from the layout
            listView = (ListView)findViewById(R.id.list);
            btn_prev     = (Button)findViewById(R.id.prev);
            btn_next     = (Button)findViewById(R.id.next);

            btn_prev.setEnabled(false);

            btn_next.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    increment++;
                    loadList(increment);
                    CheckEnable();
                }
            });

            btn_prev.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    increment--;
                    loadList(increment);
                    CheckEnable();
                }
            });

            populateList();
        }

        /**
         * Method for enabling and disabling Buttons
         */
        private void CheckEnable()
        {
            if(increment+1 == pageCount)
            {
                btn_next.setEnabled(false);
            }
            else if(increment == 0)
            {
                btn_prev.setEnabled(false);
            }
            else
            {
                btn_prev.setEnabled(true);
                btn_next.setEnabled(true);
            }
        }

        /**
         * Method for loading data in listview
         * @param number
         */
        private void loadList(int number)
        {
            sort = new JSONArray();
            ArrayList<String> aux = new ArrayList<String>();

            int start = number * NUM_ITEMS_PAGE;
            for(int i=start;i<(start)+NUM_ITEMS_PAGE;i++)
            {
                if(i<data.length())
                {
                    try {
                        sort.put(data.getJSONObject(i));
                        aux.add((String) data.getJSONObject(i).get("report_id"));
//System.out.println("List reports: "+sort.getJSONObject(i).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else
                {
                    break;
                }
            }
/*
            sd = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    sort);
            listview.setAdapter(sd);
*/

            Integer[] imageId = {
                    R.drawable.marker_bad,
                    R.drawable.marker_good,
                    R.drawable.marker_med

            };

            CustomList adapter = new
                    CustomList(DisplayReportsActivity.this, aux, sort, imageId);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
/*                    try {
                        Toast.makeText(DisplayReportsActivity.this, "You Clicked at " + sort.getJSONObject(position).get("report_id"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
*/
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

            CheckEnable();
        }

    protected void populateList () {

        Intent myIntent = getIntent(); // gets the previously created intent
        String reportsArrayString = myIntent.getStringExtra("reports_array"); // will return "report_id"

        try {
            data = new JSONArray(reportsArrayString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TOTAL_LIST_ITEMS = data.length();

        /**
         * this block is for checking the number of pages
         * ====================================================
         */

         int val = TOTAL_LIST_ITEMS%NUM_ITEMS_PAGE;
         val = val==0?0:1;
         pageCount = TOTAL_LIST_ITEMS/NUM_ITEMS_PAGE+val;
         /**
         * =====================================================
         */

         loadList(0);

        /*String url = "http://modulos.ibercivis.es/webservice/getreports.php";

        final JsonArrayRequest jsonRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // the response is already constructed as a JSONObject!
                        data = response;

                        TOTAL_LIST_ITEMS = response.length();

                        /**
                         * this block is for checking the number of pages
                         * ====================================================
                         *

                        int val = TOTAL_LIST_ITEMS%NUM_ITEMS_PAGE;
                        val = val==0?0:1;
                        pageCount = TOTAL_LIST_ITEMS/NUM_ITEMS_PAGE+val;
                        /**
                         * =====================================================
                         *

                        loadList(0);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(this).add(jsonRequest);*/
    }
}
