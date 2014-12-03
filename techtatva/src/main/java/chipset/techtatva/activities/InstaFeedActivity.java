package chipset.techtatva.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import chipset.techtatva.R;
import chipset.techtatva.resources.Functions;
import chipset.techtatva.resources.InstaFeedAdapter;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import static chipset.techtatva.resources.Constants.INSTA_CAPTION;
import static chipset.techtatva.resources.Constants.INSTA_CAPTION_TEXT;
import static chipset.techtatva.resources.Constants.INSTA_COMMENTS;
import static chipset.techtatva.resources.Constants.INSTA_COUNT;
import static chipset.techtatva.resources.Constants.INSTA_DATA;
import static chipset.techtatva.resources.Constants.INSTA_IMAGE;
import static chipset.techtatva.resources.Constants.INSTA_IMAGES;
import static chipset.techtatva.resources.Constants.INSTA_IMAGE_URL;
import static chipset.techtatva.resources.Constants.INSTA_LIKES;
import static chipset.techtatva.resources.Constants.INSTA_USER;
import static chipset.techtatva.resources.Constants.INSTA_USER_PROFILE;
import static chipset.techtatva.resources.Constants.INSTA_USER_USERNAME;
import static chipset.techtatva.resources.Constants.URL_INSTA;
public class InstaFeedActivity extends ActionBarActivity {
    SwipeRefreshLayout instaSwipe;
    ListView instaFeed;
    Functions functions = new Functions();
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insta_feed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_r);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.pBarOne);
        instaSwipe = (SwipeRefreshLayout) findViewById(R.id.instaSwipe);
        instaFeed = (ListView) findViewById(R.id.instaFeed);

        instaSwipe.setColorSchemeResources(R.color.green_light,
                R.color.red_light, R.color.purple, R.color.blue_light);
        instaSwipe.setRefreshing(true);
        new InstaFeedGet().execute();
        instaSwipe.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                new InstaFeedGet().execute();
            }
        });
    }

    private class InstaFeedGet extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONObject jObj = functions.instaFeed(URL_INSTA);
            return jObj;
        }

        @Override
        protected void onPostExecute(JSONObject jObj) {
            ArrayList<HashMap<String, String>> dataMap = new ArrayList<HashMap<String, String>>();
            instaSwipe.setRefreshing(false);
            try {
                JSONObject meta = jObj.getJSONObject("meta");
                int code = meta.getInt("code");
                if (code != 200) {
                    Crouton.showText(
                            InstaFeedActivity.this,
                            "Error communicating with the server. Please try after sometime",
                            Style.ALERT);
                } else {
                    JSONArray data = jObj.getJSONArray(INSTA_DATA);
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);
                        JSONObject x, y;
                        x = c.getJSONObject(INSTA_COMMENTS);
                        String cc = String.valueOf(x.getInt(INSTA_COUNT));
                        x = c.getJSONObject(INSTA_LIKES);
                        String lc = String.valueOf(x.getInt(INSTA_COUNT));
                        x = c.getJSONObject(INSTA_IMAGES);
                        y = x.getJSONObject(INSTA_IMAGE);
                        String url = y.getString(INSTA_IMAGE_URL);
                        x = c.getJSONObject(INSTA_CAPTION);
                        String text = x.getString(INSTA_CAPTION_TEXT);
                        x = c.getJSONObject(INSTA_USER);
                        String user = x.getString(INSTA_USER_USERNAME);
                        String userpic = x.getString(INSTA_USER_PROFILE);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(INSTA_COMMENTS, cc);
                        map.put(INSTA_LIKES, lc);
                        map.put(INSTA_IMAGE_URL, url);
                        map.put(INSTA_CAPTION_TEXT, text);
                        map.put(INSTA_USER_USERNAME, user);
                        map.put(INSTA_USER_PROFILE, userpic);
                        dataMap.add(map);
                        progressBar.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Crouton.showText(InstaFeedActivity.this,
                        "Error connecting to the server! Please try again",
                        Style.ALERT);
            }

            instaFeed.setAdapter(new InstaFeedAdapter(getApplicationContext(),
                    dataMap));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.action_dev) {
            startActivity(new Intent(InstaFeedActivity.this,
                    DeveloperActivity.class));
        } else if (id == R.id.action_contact) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(
                    InstaFeedActivity.this);
            dialog.setTitle("Contact Us");
            dialog.setMessage(getResources().getString(R.string.contactus));
            dialog.setCancelable(false);
            dialog.setNeutralButton(android.R.string.ok, null);
            dialog.create();
            dialog.show();
        } else if (id == R.id.action_result) {
            if (new Functions().isConnected(getApplicationContext()) == true) {
                startActivity(new Intent(InstaFeedActivity.this,
                        ResultActivity.class));
            } else {
                Crouton.showText(InstaFeedActivity.this,
                        "No Internet Connection", Style.ALERT);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}