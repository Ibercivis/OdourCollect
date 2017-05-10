package ibercivis.com.odourcollectapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

import java.util.HashMap;
import java.util.Map;


public class AddCFAActivity extends AppCompatActivity {

    TextView add_cfa_comment_textview;

    String error_check;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcfa);
    }

    public void addCFACommentRequest (View view) {

        add_cfa_comment_textview = (TextView) findViewById(R.id.cfaCommentTextView);

        if(checkInputComment()) {
            // Input data ok, so go with the request

            // Url for the webservice
            String url = getString(R.string.base_url) + "/addcfa.php";

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        System.out.println(response.toString());

                        JSONObject responseJSON = new JSONObject(response);

                        if ((int) responseJSON.get("result") == 1) {
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast;
                            CharSequence text;

                            text = "The call for action has been recorded.";
                            toast = Toast.makeText(getApplicationContext(), text, duration);
                            toast.show();

                            finish();

                        } else {
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast;
                            CharSequence text;

                            text = "Error while recording Call for Action: " + responseJSON.get("message") + ".";
                            toast = Toast.makeText(getApplicationContext(), text, duration);
                            toast.show();

                            // Clean the text fields for new entries
                            add_cfa_comment_textview.setText("");
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

                    Map<String, String> addcfacomment_params = new HashMap<String, String>();
                    addcfacomment_params.put("cfa_comment", add_cfa_comment_textview.getText().toString());
                    addcfacomment_params.put("user", session.getUsername());

                    Intent myIntent = getIntent(); // gets the previously created intent
                    int report_id = myIntent.getIntExtra("report_id", 0); // will return "report_id"

                    addcfacomment_params.put("report_id", String.valueOf(report_id));

                    return addcfacomment_params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
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

    private boolean checkLength( String text, String fieldName, int min, int max ) {
        if ( text.length() > max || text.length() < min ) {
            error_check = error_check + "Length of " + fieldName + " must be between " +
                    min + " and " + max + ".\n";
            return false;
        } else {
            return true;
        }
    }

    private boolean checkInputComment () {

        error_check = "";
        boolean valid = true;

        // valid is evaluated in the second part to force the check function being called always, so all the errors are displayed at the same time (&& conditional evaluation)
        valid = checkLength( add_cfa_comment_textview.getText().toString(), "cfa_comment", 2, 255 ) && valid;

        if (!error_check.equals("")){
            showError(error_check);
        }

        return valid;
    }

    /** Cancel Add Comment Activity */
    public void cancelAddCFAComment(View view) {
        finish();
    }
}
