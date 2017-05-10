package ibercivis.com.odourcollectapp;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CustomList extends ArrayAdapter<String>{

    private final Activity context;
    private final JSONArray web;
    private final Integer[] imageId;
    public CustomList(Activity context, ArrayList<String> aux, JSONArray web, Integer[] imageId) {

        super(context, R.layout.list_single, aux);
        this.context = context;
        this.web = web;
        this.imageId = imageId;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.list_txt);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_img);

        JSONObject odourRecord = null;

        try {
            /* Get the JSON object from the JSON array */
            odourRecord = web.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            /* Create info to display in the pop up */
            String odourRecordString = "User, date: "+odourRecord.get("username").toString()+" on "+odourRecord.get("report_date").toString()
                    +"\nType: "+odourRecord.get("type").toString()
                    +"\nAnnoyance (-4 - 4): "+odourRecord.get("annoyance").toString()+", Intensity (1 - 6): "+odourRecord.get("intensity").toString()
                    +"\nNumber of comments: "+odourRecord.get("number_comments").toString();

            txtTitle.setText(odourRecordString);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (Integer.parseInt(odourRecord.get("annoyance").toString()) > 2) {
                imageView.setImageResource(imageId[0]);
            } else if (Integer.parseInt(odourRecord.get("annoyance").toString()) < -2) {
                imageView.setImageResource(imageId[1]);
            }
            else {
                imageView.setImageResource(imageId[2]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rowView;
    }
}