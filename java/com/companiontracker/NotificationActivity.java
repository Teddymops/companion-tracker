package com.companiontracker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.companiontracker.model.InvitePeopleModel;
import com.companiontracker.utility.Networking;
import com.companiontracker.utility.Preferences;
import com.companiontracker.viewholder.NotificationViewHolder;

public class NotificationActivity extends BaseActivity {

    private TextView mToolbarTitleTextView;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private FirebaseDatabase database;

    private FirebaseRecyclerAdapter<InvitePeopleModel, NotificationViewHolder> mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_connections);

        database = FirebaseDatabase.getInstance();

        configureToolbar();

        mRecycler = (RecyclerView) findViewById(R.id.users_list);
        mRecycler.setHasFixedSize(true);
        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        if (mToolbarTitleTextView != null) {
            //Setting name for toolbar
            mToolbarTitleTextView.setText("Notifications");
        }

        if (Networking.isNetworkAvailable(NotificationActivity.this)) {

            getNotifications();

        } else {
            showSnackBar(getResources().getString(R.string.no_connection));
            showDialog_singleButton(getResources().getString(R.string.no_connection));
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void configureToolbar() {
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mainToolbar != null) {
            mToolbarTitleTextView = (TextView) mainToolbar.findViewById(R.id.toolbar_title_textView);

            setSupportActionBar(mainToolbar);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

    }

    private void getNotifications() {

        final String myKey = new Preferences().getUserKey(NotificationActivity.this);
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("notification");
        Query postsQuery = myRef.child(myKey);
        mAdapter = new FirebaseRecyclerAdapter<InvitePeopleModel, NotificationViewHolder>(InvitePeopleModel.class,
                R.layout.item_notification,
                NotificationViewHolder.class, postsQuery) {

            @Override
            protected void populateViewHolder(NotificationViewHolder viewHolder, final InvitePeopleModel model,
                                              int position) {

                final DatabaseReference postRef = getRef(position);
                final String friend_key = postRef.getKey();

                if (model != null) {
                    viewHolder.bindToNotification(NotificationActivity.this, model,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //on Yes pressed //Friend Request accepted

                                    //Adding connection to My connections
                                    InvitePeopleModel inviteModel = new InvitePeopleModel
                                            (model.user_name, model.user_email,
                                                    "inviteAccepted", true, true, true, "");
                                    DatabaseReference my_conn_ref = database.getReference("my_connections");
                                    my_conn_ref.child(myKey).child(friend_key).setValue(inviteModel);


                                    //Adding My connections to connection
                                    Preferences myPref = new Preferences();
                                    InvitePeopleModel myDetailsModel = new InvitePeopleModel
                                            (myPref.getUserName(NotificationActivity.this),
                                                    myPref.getEmail(NotificationActivity.this),
                                                    "inviteAccepted", true, true, true, "");
                                    my_conn_ref.child(friend_key).child(myKey).setValue(myDetailsModel);

                                    //Removing from sent request
                                    DatabaseReference my_iRef = database.getReference("sent_request");
                                    my_iRef.child(friend_key).child(myKey).removeValue();

                                    //Removing from notification
                                    DatabaseReference my_NotfiRef = database.getReference("notification");
                                    my_NotfiRef.child(myKey).child(friend_key).removeValue();

                                }
                            },
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //on No pressed //Request Canceled
                                    //Removing from sent request
                                    DatabaseReference my_iRef = database.getReference("sent_request");
                                    my_iRef.child(friend_key).child(myKey).removeValue();

                                    //Removing from notification
                                    DatabaseReference my_NotfiRef = database.getReference("notification");
                                    my_NotfiRef.child(myKey).child(friend_key).removeValue();
                                }
                            });
                } else {
                    Toast.makeText(NotificationActivity.this, "No Notifications", Toast.LENGTH_SHORT).show();
                }

            }
        };

        mRecycler.setAdapter(mAdapter);
    }


}
