package com.example.android.griesol;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class AddDepartment extends AppCompatActivity {

    EditText editDept;
    EditText editVenue;
    EditText editAsset;
    TextView textVenue;
    TextView textAsset;
    TextView titleView;
    ProgressBar progressBar;
    ArrayList<String> venueList = new ArrayList<>();
    ArrayList<String> assetList = new ArrayList<>();
    boolean update;
    Intent receivedIntent;
    JSONObject receivedObject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_department);
        receivedIntent = getIntent();
        if (receivedIntent.hasExtra("department")) {
            update = true;
//            getSupportActionBar().setTitle("Update Department");
        } else {
            update = false;
//            getSupportActionBar().setTitle("Add Department");
        }
        editDept = (EditText)findViewById(R.id.editDept);
        editVenue = (EditText)findViewById(R.id.editVenue);
        editAsset = (EditText)findViewById(R.id.editAsset);
        textVenue = (TextView) findViewById(R.id.textVenue);
        textAsset = (TextView) findViewById(R.id.textAsset);
        titleView = (TextView) findViewById(R.id.add_dept_title);
        progressBar = findViewById(R.id.add_dept_loading_pb);
        if (update) {
            titleView.setText("Update Department");
            try {
                initFields();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void initFields() throws Exception{
        receivedObject = new JSONObject(receivedIntent.getStringExtra("department"));
        editDept.setText(receivedObject.getString("name"));
        JSONArray venues = receivedObject.getJSONArray("venues");
        JSONArray assets = receivedObject.getJSONArray("assets");
        for (int i = 0; i < venues.length(); i++) {
            venueList.add(venues.getString(i));
            textVenue.append((i+1) + ") " + venues.getString(i) + "\n");
        }
        for (int i = 0; i < assets.length(); i++) {
            assetList.add(assets.getString(i));
            textAsset.append((i+1) + ") " + assets.getString(i) + "\n");
        }
    }


    public void addVenue(View view) {
        String venue = editVenue.getText().toString();
        if (venue.length() == 0){
            return;
        }
        venueList.add(venue);
        int i = venueList.size();
        textVenue.append(i + ") " + venue + "\n");
        editVenue.setText("");
    }

    public void addAsset(View view) {
        String asset = editAsset.getText().toString();
        if (asset.length() == 0){
            return;
        }
        assetList.add(asset);
        int i = assetList.size();
        textAsset.append(i + ") " + asset + "\n");
        editAsset.setText("");
    }

    public void clearAll(View view) {
        assetList.clear();
        venueList.clear();
        textVenue.setText("");
        textAsset.setText("");
        editAsset.setText("");
        editVenue.setText("");
    }

    public void uploadDepartment(View view) throws Exception{
        String deptName = editDept.getText().toString();
        if (deptName.length() == 0){
            Toast.makeText(this, "Enter the Department name", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        JSONArray jsonArrayVenue = new JSONArray(venueList);
        JSONArray jsonArrayAsset = new JSONArray(assetList);
        JSONObject deptObj = new JSONObject();
        deptObj.put("name",deptName);
        deptObj.put("assets",jsonArrayAsset);
        deptObj.put("venues",jsonArrayVenue);
        if (update){
            deptObj.put("_id",receivedObject.get("_id"));
        }
        Log.d(TAG, "uploadDepartment: "+deptObj);
        addDepartmentPost(deptObj);
    }

    private void addDepartmentPost(JSONObject deptObj) {
        OkHttpClient client = new OkHttpClient();
        String url;
        if (update){
            url = "https://grie-sol.herokuapp.com/department/update";
        }else{
            url = "https://grie-sol.herokuapp.com/department/add";
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), deptObj.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                AddDepartment.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                AddDepartment.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
                if (response.isSuccessful()){
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: "+ myResponse);

                    AddDepartment.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddDepartment.this, "Department is uploaded", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

                }
            }
        });
    }
}