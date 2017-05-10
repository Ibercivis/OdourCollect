package ibercivis.com.odourcollectapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class SignUpActivity extends AppCompatActivity {

        TextView signup_username_textview;
        TextView signup_email_textview;
        TextView signup_password_textview;
        Spinner age_spinner;
        Spinner gender_spinner;

        String error_check;

        int selected, age_spinner_item, gender_spinner_item;

        /* http://www.androidhive.info/2012/01/android-login-and-registration-with-php-mysql-and-sqlite/ */

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_signup);


            age_spinner = (Spinner) findViewById(R.id.age_spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> age_adapter = ArrayAdapter.createFromResource(this,
                    R.array.age_array, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            age_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            age_spinner.setAdapter(age_adapter);
            //  Make spinner "Select Age" item not selectable by overriding listener
            age_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    selected = age_spinner.getSelectedItemPosition();
                    if (selected != 0)
                        age_spinner_item = selected;
                    System.out.println(selected);

                    setIdAge();
                }

                private void setIdAge() {
                    age_spinner.setSelection(age_spinner_item);
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

            gender_spinner = (Spinner) findViewById(R.id.gender_spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> gender_adapter = ArrayAdapter.createFromResource(this,
                    R.array.gender_array, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            gender_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            gender_spinner.setAdapter(gender_adapter);
            //  Make spinner "Select Gender" item not selectable by overriding listener
            gender_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    selected = gender_spinner.getSelectedItemPosition();
                    if (selected != 0)
                        gender_spinner_item = selected;
                    System.out.println(selected);

                    setIdGender();
                }

                private void setIdGender() {
                    gender_spinner.setSelection(gender_spinner_item);
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

        }

    public void signupRequest (View view) {

        signup_username_textview = (TextView) findViewById(R.id.signup_username);
        signup_email_textview = (TextView) findViewById(R.id.signup_email);
        signup_password_textview = (TextView) findViewById(R.id.signup_password);

        if(checkInputSignup()) {
            // Input data ok, so go with the request

            // Url for the webservice
            String url = getString(R.string.base_url) + "/signup.php";

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        System.out.println(response.toString());

                        JSONObject responseJSON = new JSONObject(response);

                        if ((int) responseJSON.get("result") == 1){
                            SessionManager session = new SessionManager(getApplicationContext());
                            session.setLogin(true, signup_username_textview.getText().toString());

                            finish();

                        }
                        else {
                            showError("Error while signing up: " + responseJSON.get("message") + ".");

                            // Clean the text fields for new entries
                            signup_username_textview.setText("");
                            signup_email_textview.setText("");
                            signup_password_textview.setText("");
                            age_spinner_item = 0;
                            age_spinner.setSelection(age_spinner_item);
                            gender_spinner_item = 0;
                            gender_spinner.setSelection(gender_spinner_item);
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
            }){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> signup_params = new HashMap<String, String>();
                    signup_params.put("username", signup_username_textview.getText().toString());
                    signup_params.put("email", signup_email_textview.getText().toString());
                    signup_params.put("password", signup_password_textview.getText().toString());
                    signup_params.put("age", age_spinner.getSelectedItem().toString());
                    signup_params.put("gender", gender_spinner.getSelectedItem().toString());

                    return signup_params;
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

    private boolean checkLength( String text, String fieldName, int min, int max ) {
        if ( text.length() > max || text.length() < min ) {
            error_check = error_check + "Length of " + fieldName + " must be between " +
                    min + " and " + max + ".\n";
            return false;
        } else {
            return true;
        }
    }

    private boolean checkRegexp(String text, Pattern regexp, String errorMessage) {
        if (!regexp.matcher(text).matches()) {
            error_check = error_check + errorMessage + "\n";
            return false;
        } else {
            return true;
        }
    }

    private boolean checkSelect(Spinner spinner, String errorMessage) {

        if (spinner.getSelectedItemPosition() == 0) {
            error_check = error_check + "You must select one of the options from the " + errorMessage + " drop-down.\n";
            return false;
        }
        return true;
    }

    private boolean checkInputSignup () {

        error_check = "";
        boolean valid = true;
        String emailRegex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        // valid is evaluated in the second part to force the check function being called always, so all the errors are displayed at the same time (&& conditional evaluation)
        valid = checkLength( signup_username_textview.getText().toString(), "username", 3, 16 ) && valid;
        valid = checkLength( signup_email_textview.getText().toString(), "email", 6, 80 ) && valid;
        valid = checkLength( signup_password_textview.getText().toString(), "password", 5, 16 ) && valid;
//"/^[a-z]([0-9a-z_ ])+$/i"
        // In the regular expression for the username and password we do not use {3,16} (for instance),
        // to control the length through the regex, since it is most accurate to indicate the length error
        // separately, so it is not considered the length in the regex (it has been taken into account previously
        valid = checkRegexp( signup_username_textview.getText().toString(), Pattern.compile("^[a-zA-Z][a-zA-Z0-9 _]+$"), "Username may consist of a-z, 0-9, underscores, spaces and must begin with a letter." ) && valid;
        valid = checkRegexp( signup_email_textview.getText().toString(), Pattern.compile(emailRegex), "Wrong email address, eg. user@odourcollect.com" ) && valid;
        valid = checkRegexp( signup_password_textview.getText().toString(), Pattern.compile("^[0-9a-zA-Z]+$"), "Password field only allow : a-z 0-9") && valid;

        valid = checkSelect( age_spinner, "age" ) && valid;
        valid = checkSelect( gender_spinner, "gender" ) && valid;

        if (!error_check.equals("")){
            showError(error_check);
        }

        return valid;
    }

    /** Open Login Activity */
    public void openLoginSUView(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
