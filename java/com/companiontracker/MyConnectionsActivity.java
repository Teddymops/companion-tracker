package com.companiontracker;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.companiontracker.interfaces.InviteUserAdapterInterface;
import com.companiontracker.model.InvitePeopleModel;
import com.companiontracker.model.UserContactModel;
import com.companiontracker.utility.Networking;
import com.companiontracker.utility.Preferences;
import com.companiontracker.viewholder.MyConnectionsViewHolder;

import java.util.HashMap;


public class MyConnectionsActivity extends BaseActivity implements InviteUserAdapterInterface,
        SearchView.OnQueryTextListener, SearchView.OnCloseListener, View.OnClickListener {

    //Global mutable views
    private TextView mToolbarTitleTextView;
    private ProgressBar mProgressBar;
    private TextView mMessageTextView;
    private Button mRetryButton;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    //FireBase
    private FirebaseDatabase database;

    //Firebase Adapter
    private FirebaseRecyclerAdapter<InvitePeopleModel, MyConnectionsViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_connections);

        database = FirebaseDatabase.getInstance();
        configureToolbar();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageTextView = (TextView) findViewById(R.id.message_textView);
        mRetryButton = (Button) findViewById(R.id.retry_button);
        mRecycler = (RecyclerView) findViewById(R.id.users_list);

        mRecycler.setHasFixedSize(true);
        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        if (mToolbarTitleTextView != null) {
            //Setting name for toolbar
            mToolbarTitleTextView.setText(getString(R.string.my_connections));
        }

        mRetryButton.setOnClickListener(this);

        //Check network before getting my connections //TODO handle if no connections
        getConnections(false, "");

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

    String myKey;

    private SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invite, menu);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(MyConnectionsActivity.this);
        searchView.setOnCloseListener(MyConnectionsActivity.this);

        return true;

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

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Don't care about this.
        return false;
    }

    @Override
    public boolean onQueryTextChange(String queryText) {

        if (!TextUtils.isEmpty(queryText) && queryText.length() >= 1) {


            if(mMessageTextView.getVisibility() == View.GONE &&
                    !mMessageTextView.getText().toString().equals(getString(R.string.no_connection)) ||
                   !mMessageTextView.getText().toString().equals(getString(R.string.could_not_find))){
                getConnections(true, queryText);
            }
            return true;
        } else {
            getConnections(false, "");

            return false;
        }
    }


    @Override
    public boolean onClose() {
        return false;
    }

    private void getConnections(boolean isSearch, String value) {

        if (Networking.isNetworkAvailable(MyConnectionsActivity.this)) {


            Query postsQuery;
            mRecycler.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            myKey = new Preferences().getUserKey(MyConnectionsActivity.this);

            if (isSearch) {
                if (mAdapter != null) {
                    mAdapter.cleanup();
                }

                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("my_connections");
                //uF8FF - Encoding
                postsQuery = myRef.child(myKey).orderByChild("user_name").startAt(value).endAt(value + "\uF8FF");

            } else {

                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("my_connections");
                postsQuery = myRef.child(myKey);
            }

            mAdapter = new FirebaseRecyclerAdapter<InvitePeopleModel, MyConnectionsViewHolder>
                    (InvitePeopleModel.class,
                            R.layout.item_myconnections,
                            MyConnectionsViewHolder.class, postsQuery) {
                @Override
                protected void populateViewHolder(MyConnectionsViewHolder viewHolder,
                                                  final InvitePeopleModel model, int position) {
                    final DatabaseReference postRef = getRef(position);
                    final String friend_key = postRef.getKey();

                    if (model != null) {
                        viewHolder.bindToResponse(MyConnectionsActivity.this, model,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        //Creating the instance of PopupMenu
                                        PopupMenu popup = new PopupMenu(MyConnectionsActivity.this, view);
                                        //Inflating the Popup using xml file
                                        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                                        //registering popup with OnMenuItemClickListener
                                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            public boolean onMenuItemClick(MenuItem item) {

                                        Toast.makeText(MyConnectionsActivity.this, "You Clicked : " + item.getTitle(),
                                                        Toast.LENGTH_SHORT).show();
                                                return true;
                                            }
                                        });

                                        popup.show();//showing popup menu

                                    }
                                }, new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                                        //set the switch to ON
                                        // mySwitch.setChecked(true);
                                        if (isChecked) {

                                            HashMap<String, Object> result = new HashMap<>();
                                            result.put("access_my_location", true);
                                            DatabaseReference my_NotfiRef = database.getReference("my_connections");
                                            my_NotfiRef.child(myKey).child(friend_key).updateChildren(result);

                                            HashMap<String, Object> frndlocation = new HashMap<>();
                                            frndlocation.put("track_permission", true);
                                            my_NotfiRef.child(friend_key).child(myKey).updateChildren(frndlocation);

                                            //switchStatus.setText("Switch is currently ON");
                                            Toast.makeText(getApplicationContext(), "Location Sharing is ON", Toast.LENGTH_SHORT).show();
                                        } else {

                                            HashMap<String, Object> result = new HashMap<>();
                                            result.put("access_my_location", false);
                                            DatabaseReference my_NotfiRef = database.getReference("my_connections");
                                            my_NotfiRef.child(myKey).child(friend_key).updateChildren(result);


                                            HashMap<String, Object> frndlocation = new HashMap<>();
                                            frndlocation.put("track_permission", false);
                                            my_NotfiRef.child(friend_key).child(myKey).updateChildren(frndlocation);


                                            // switchStatus.setText("Switch is currently OFF");
                                            Toast.makeText(getApplicationContext(), "Location Sharing is OFF", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            };
            mRecycler.setAdapter(mAdapter);

            postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        //TODO CHANGE to no connections or to invite
                        mRecycler.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.GONE);
                        mRetryButton.setVisibility(!Networking.isNetworkAvailable(MyConnectionsActivity.this) ?
                        View.VISIBLE:View.GONE);
                        mMessageTextView.setVisibility(View.VISIBLE);
                        mMessageTextView.setText(!Networking.isNetworkAvailable(MyConnectionsActivity.this) ?
                                getString(R.string.no_connection):getString(R.string.could_not_find));

                    } else {
                        mRecycler.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        mMessageTextView.setVisibility(View.GONE);
                        mRetryButton.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            noConnection(getString(R.string.no_connection));
        }
    }

    @Override
    public void onInviteFriend(UserContactModel selectedFriend) {

    }

    @Override
    public void onClick(View v) {
        getConnections(false, "");

    }

    private void noConnection(String message) {
        mRecycler.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.VISIBLE);
        mMessageTextView.setVisibility(View.VISIBLE);
        mMessageTextView.setText(message);
        showSnackBar(getResources().getString(R.string.no_connection));
        showDialog_singleButton(getResources().getString(R.string.no_connection));
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }
}
