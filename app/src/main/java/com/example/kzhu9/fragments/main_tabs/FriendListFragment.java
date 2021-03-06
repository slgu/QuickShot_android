package com.example.kzhu9.fragments.main_tabs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.kzhu9.config.Config;
import com.example.kzhu9.myapplication.FriendInfo;
import com.example.kzhu9.myapplication.FriendItemClickListener;
import com.example.kzhu9.myapplication.FriendItemLongClickListener;
import com.example.kzhu9.myapplication.FriendList;
import com.example.kzhu9.myapplication.FriendListAdapter;
import com.example.kzhu9.myapplication.R;
import com.example.kzhu9.myapplication.okhttp_singleton.OkHttpSingleton;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by jinliang on 11/15/15.
 */
public class FriendListFragment extends Fragment implements FriendItemClickListener, FriendItemLongClickListener, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private FriendListAdapter adapter;
    private SwipeRefreshLayout swipeContainer;

    final ArrayList<FriendList.FriendEntity> friList = new ArrayList<FriendList.FriendEntity>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listview, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.friendList);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer_friend);
        System.out.println("createview");
//        try {
//            Thread.sleep(500);
//            swipeContainer.post(new Runnable() {
//                @Override
//                public void run() {
//                    swipeContainer.setRefreshing(true);
//                    dosomething();
//                }
//            });
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeContainer.setOnRefreshListener(this);

        adapter = new FriendListAdapter(getActivity().getApplicationContext());

        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setHasFixedSize(true);

        System.out.println("activity created");
        getFriendUidList();
    }

    public void getFriendUidList() {

        String requestURL = Config.REQUESTURL + "/user/get";

        RequestBody formBody = new FormEncodingBuilder()
                .add("uid", Config.user_id)
                .build();
        Request request = new Request.Builder()
                .url(requestURL)
                .post(formBody)
                .build();

        if (getActivity() == null)
            return;

        OkHttpSingleton.getInstance().getClient(getActivity().getApplicationContext()).newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException throwable) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(getActivity(), "Unable to connect to server, please try later", Toast.LENGTH_LONG).show();
                    }
                });
                throwable.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);

                String responseStr = response.body().string();

                try {
                    JSONObject responseObj = new JSONObject(responseStr);
                    int status = Integer.parseInt(responseObj.get("status").toString());

                    switch (status) {
                        case 0:
                            JSONObject info = responseObj.getJSONObject("info");
                            JSONArray friendsListObj = info.getJSONArray("friends_list");

                            ArrayList<String> uidList = new ArrayList<>();

                            for (int i = 0; i < friendsListObj.length(); i++) {
                                uidList.add(friendsListObj.getString(i));
                            }
                            getFriendList(uidList);
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void dosomething() {
        swipeContainer.setRefreshing(true);
        getFriendUidList();

        if (getActivity() == null)
            return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(getActivity(), "My Friends Refreshed!", Toast.LENGTH_LONG).show();
            }
        });

        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        dosomething();
    }

    public void getFriendList(ArrayList<String> friendUidList) {
        final int size = friendUidList.size();

        for (String uid : friendUidList) {
            String requestURL = Config.REQUESTURL + "/user/get";

            RequestBody formBody = new FormEncodingBuilder()
                    .add("uid", uid)
                    .build();
            Request request = new Request.Builder()
                    .url(requestURL)
                    .post(formBody)
                    .build();
            if (getActivity() == null)
                return;

            OkHttpSingleton.getInstance().getClient(getActivity().getApplicationContext()).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException throwable) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getActivity(), "Unable to connect to server server, please try later", Toast.LENGTH_LONG).show();
                        }
                    });
                    throwable.printStackTrace();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    String responseStr = response.body().string();
                    System.out.println("responseStr "+responseStr);

                    JSONObject friendList;
                    JSONObject info;

                    try {
                        FriendList.FriendEntity friendEntity = new FriendList.FriendEntity();

                        friendList = new JSONObject(responseStr);
                        info = friendList.getJSONObject("info");
                        friendEntity.setEmail(info.getString("email"));
                        friendEntity.setName(info.getString("name"));
                        friendEntity.setTopics_list(info.getString("topics_list"));
                        friendEntity.setSex(info.getInt("sex"));
                        friendEntity.setAge(info.getInt("age"));
                        friendEntity.setAddress(info.getString("address"));
                        friendEntity.setImg_uid(info.getString("img_uid"));

                        friList.add(friendEntity);

                        if(getActivity() == null)
                            return;

                        if (friList.size() == size) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Collections.sort(friList);
                                    adapter.setList(friList);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(getActivity(), FriendInfo.class);

        intent.putExtra("NAME", friList.get(position).getName());
        intent.putExtra("AGE", friList.get(position).getAge());
        intent.putExtra("EMAIL", friList.get(position).getEmail());
        intent.putExtra("ADDRESS", friList.get(position).getAddress());
        intent.putExtra("TOPIC_LISTS", friList.get(position).getTopics_list());
        intent.putExtra("IMG_UID", friList.get(position).getImg_uid());

        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
}
