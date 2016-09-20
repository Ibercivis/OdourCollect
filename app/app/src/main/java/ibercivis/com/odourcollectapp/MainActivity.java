package ibercivis.com.odourcollectapp;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.TabLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/* Tabs code from: http://www.androidhive.info/2015/09/android-material-design-working-with-tabs/ */
public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    MainFragment mainFragmentInstance;

    private DatePicker datePicker;
    private Calendar calendar;
    private int year, month, day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permissions to support Android Marshmallow and above devices
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //getSupportActionBar().setIcon(R.mipmap.brand_logo);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        mainFragmentInstance = new MainFragment();
        adapter.addFragment(mainFragmentInstance, "Reports");
        adapter.addFragment(new ProjectFragment(), "The Project");
        adapter.addFragment(new MethodologyFragment(), "Methodology");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast;
        CharSequence text;
        Intent intent;
        SessionManager session;

        switch (item.getItemId()) {
            case R.id.action_add_report:
                // User chose the "Settings" item, show the app settings UI...

                session = new SessionManager(this);
                if(session.isLoggedIn())
                {
                    // User logged in, launch add activity
                    addReportActivity(this.findViewById(android.R.id.content));
                }
                else
                {
                    // User not logged in, launch login activity
                    text = "You have to be logged in to add or comment reports!";
                    toast = Toast.makeText(context, text, duration);
                    toast.show();

                    openLogin(this.findViewById(android.R.id.content));
                }

                return true;

            case R.id.menu_filter_type_garbage:
                // User chose the "Settings" item, show the app settings UI...

                mainFragmentInstance.filterType("Garbage");

                return true;

            case R.id.menu_filter_type_sewage:
                // User chose the "Settings" item, show the app settings UI...

                mainFragmentInstance.filterType("Sewage");

                return true;

            case R.id.menu_filter_type_chemical:
                // User chose the "Settings" item, show the app settings UI...

                mainFragmentInstance.filterType("Chemical");

                return true;

            case R.id.menu_filter_type_dont_know:
                // User chose the "Settings" item, show the app settings UI...

                mainFragmentInstance.filterType("Do not know");

                return true;

            case R.id.menu_filter_type_other:
                // User chose the "Settings" item, show the app settings UI...

                mainFragmentInstance.filterType("Other");

                return true;

            case R.id.menu_filter_since:
                // User chose the "Settings" item, show the app settings UI...

//mainFragmentInstance.filterDateSince("2016-09-14 19:37:29");
                showDialog(1);

                return true;

            case R.id.menu_filter_until:
                // User chose the "Settings" item, show the app settings UI...

//mainFragmentInstance.filterDateUntil("2016-09-17 19:37:29");
                showDialog(2);

                return true;

            case R.id.menu_filter_remove:

                mainFragmentInstance.removeFilters();

                return true;

            case R.id.action_login:

                session = new SessionManager(this);
                if(session.isLoggedIn())
                {
                    text = "You are already logged in.";
                    toast = Toast.makeText(context, text, duration);
                    toast.show();


                }
                else
                {
                    openLogin(this.findViewById(android.R.id.content));
                }

                return true;

            case R.id.action_success:
                // User chose the "Settings" item, show the app settings UI...

                intent = new Intent(this, SuccessActivity.class);
                startActivity(intent);

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    // START PERMISSION CHECK
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        String message = "osmdroid permissions:";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            message += "\nLocation to show user location.";
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            message += "\nStorage access to store map tiles.";
        }
        if (!permissions.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } // else: We already have permissions, so handle as normal
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION and WRITE_EXTERNAL_STORAGE
                Boolean location = perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (location && storage) {
                    // All Permissions Granted
                    Toast.makeText(MainActivity.this, "All permissions granted", Toast.LENGTH_SHORT).show();
                } else if (location) {
                    Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
                } else if (storage) {
                    Toast.makeText(this, "Location permission is required to show the user's location on map.", Toast.LENGTH_LONG).show();
                } else { // !location && !storage case
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Storage permission is required to store map tiles to reduce data usage and for offline usage." +
                            "\nLocation permission is required to show the user's location on map.", Toast.LENGTH_SHORT).show();
                }
                viewPager = (ViewPager) findViewById(R.id.viewpager);
                setupViewPager(viewPager);
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // END PERMISSION CHECK

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 1) {
            return new DatePickerDialog(this, myDateListenerSince, year, month, day);
        }
        else if (id == 2) {
            return new DatePickerDialog(this, myDateListenerUntil, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListenerSince = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            String auxDate = new String(arg1+"-"+(arg2+1)+"-"+arg3+" 00:00:00");
            mainFragmentInstance.filterDateSince(auxDate);
        }
    };

    private DatePickerDialog.OnDateSetListener myDateListenerUntil = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            String auxDate = new String(arg1+"-"+(arg2+1)+"-"+arg3+" 00:00:00");
            System.out.println("Filtering until: "+auxDate);
            mainFragmentInstance.filterDateUntil(auxDate);
        }
    };

    // INTENTS

    /** Display Reports Activity */
    public void displayReportsActivity(View view) {
        Intent intent = new Intent(this, DisplayReportsActivity.class);
        intent.putExtra("reports_array", mainFragmentInstance.getReportsArray().toString());
        startActivity(intent);
    }

    /** Open Login Activity */
    public void openLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /** Add Report Activity */
    public void addReportActivity(View view) {
        Intent intent = new Intent(this, AddReportActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                mainFragmentInstance.populateOverlay();
            }
        }
    }//onActivityResult
}