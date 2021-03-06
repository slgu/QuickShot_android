package com.example.kzhu9.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kzhu9.cache.ImgCache;
import com.example.kzhu9.config.Config;
import com.example.kzhu9.fragments.sidebar.CreateTopicsFragment;
import com.example.kzhu9.fragments.sidebar.MainFragment;
import com.example.kzhu9.fragments.sidebar.NotificationFragment;
import com.example.kzhu9.fragments.sidebar.RecommendationFragment;
import com.example.kzhu9.fragments.sidebar.SearchTopicsFragment;
import com.example.kzhu9.fragments.sidebar.SearchUsersFragment;
import com.example.kzhu9.myapplication.okhttp_singleton.OkHttpSingleton;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        TextView profileName = (TextView) header.findViewById(R.id.profile_name);
        profileName.setText(SelfInfo.name);

        String uid = SelfInfo.img_uid;
        Bitmap cacheRes = ImgCache.single().get(uid);
        if (cacheRes == null) {
            new DownloadImage().setHeader(header).setUrl(uid).execute(uid);
        }
        else {
            ImageView img = (ImageView) header.findViewById(R.id.profile_pic);
            img.setImageBitmap(Bitmap.createScaledBitmap(cacheRes, 180, 180, false));
        }

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new MainFragment()).commit();
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        View header;
        public DownloadImage setHeader(View header) {
            this.header = header;
            return this;
        }
        String url;
        public DownloadImage setUrl(String url) {
            this.url = url;
            return this;
        }
        protected Bitmap doInBackground(String... urls) {
            return getBitmapFromURL(urls[0]);
        }
        protected void onPostExecute(Bitmap result) {
            //set cache
            ImgCache.single().put(url, result);
            ImageView img = (ImageView) header.findViewById(R.id.profile_pic);
            img.setImageBitmap(Bitmap.createScaledBitmap(result, 180, 180, false));
        }
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (exit) {
                finish(); // finish activity
            } else {
                Toast.makeText(this, "Press Back again to Exit.",
                        Toast.LENGTH_SHORT).show();
                exit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 3 * 1000);
            }
        }
    }

    static Button notifCount;
    static int mNotifCount = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), FriendInfo.class);
                intent.putExtra("NAME", SelfInfo.name);
                intent.putExtra("SEX", SelfInfo.sex);
                intent.putExtra("AGE", SelfInfo.age);
                intent.putExtra("EMAIL", SelfInfo.email);
                intent.putExtra("ADDRESS", SelfInfo.address);
                intent.putExtra("TOPIC_LISTS", SelfInfo.topics_list);
                intent.putExtra("IMG_UID", SelfInfo.img_uid);

                startActivity(intent);
            }
        });
//
//        MenuItem item = menu.findItem(R.id.badge);
//        MenuItemCompat.setActionView(item, R.layout.feed_update_count);
//        notifCount = (Button) MenuItemCompat.getActionView(item);
//
//        // change notification number
//        notifCount.setText(String.valueOf(mNotifCount));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();

        int id = item.getItemId();

        if (id == R.id.nav_mainFragment) {
            fm.beginTransaction().replace(R.id.content_frame, new MainFragment()).commit();
        } else if (id == R.id.nav_searchFriends) {
            fm.beginTransaction().replace(R.id.content_frame, new SearchUsersFragment()).commit();
        } else if (id == R.id.nav_searchTopics) {
            fm.beginTransaction().replace(R.id.content_frame, new SearchTopicsFragment()).commit();
        } else if (id == R.id.nav_createTopics) {
            fm.beginTransaction().replace(R.id.content_frame, new CreateTopicsFragment()).commit();
        } else if (id == R.id.nav_notifications) {
            fm.beginTransaction().replace(R.id.content_frame, new NotificationFragment()).commit();
        } else if (id == R.id.nav_recommendations) {
            fm.beginTransaction().replace(R.id.content_frame, new RecommendationFragment()).commit();
        } else if (id == R.id.nav_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void logout() {
        String requestURL = Config.REQUESTURL  + "/user/logout";

        RequestBody formBody = new FormEncodingBuilder()
                .build();

        Request request = new Request.Builder()
                .url(requestURL)
                .post(formBody)
                .build();

        OkHttpSingleton.getInstance().getClient(getApplicationContext()).newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    SelfInfo.clear();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // return to login
                            Toast.makeText(getApplicationContext(), "Server is down, Relogin Please!", Toast.LENGTH_LONG).show();
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                }

                String responseStr = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseStr);
                    switch (jsonObject.getInt("status")) {
                        case 0:
                            SelfInfo.clear();

                            startActivity(new Intent(MainActivity.this, LoginActivity.class));

                            finish();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Successfully logout!", Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
