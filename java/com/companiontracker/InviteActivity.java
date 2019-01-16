package com.companiontracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.companiontracker.model.InvitePeopleModel;
import com.companiontracker.model.UserModel;
import com.companiontracker.utility.Networking;
import com.companiontracker.utility.Preferences;
import com.companiontracker.viewholder.InviteViewHolder;


public class InviteActivity extends BaseActivity implements View.OnClickListener,
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    SearchView searchView;
    private TextView mToolbarTitleTextView;
    private FirebaseRecyclerAdapter<UserModel, InviteViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private RelativeLayout mSearchViewRelativeLayout;
    private Button mSearchButton, mContactsButton;
    private TextView mSkipTextView;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_people);

        database = FirebaseDatabase.getInstance();

        configureToolbar();
        //setSearchView();

        mSearchViewRelativeLayout = (RelativeLayout) findViewById(R.id.searchView_relativeLayout);
        mSearchButton = (Button) findViewById(R.id.search_button);
        mContactsButton = (Button) findViewById(R.id.contacts_button);
        mSkipTextView = (TextView) findViewById(R.id.skip_textView);
        mRecycler = (RecyclerView) findViewById(R.id.users_list);
        mRecycler.setHasFixedSize(true);


        mSearchButton.setOnClickListener(this);
        mContactsButton.setOnClickListener(this);
        mSkipTextView.setOnClickListener(this);

        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);


        if (mToolbarTitleTextView != null) {
            //Setting name for toolbar
            mToolbarTitleTextView.setText("Invite");
        }
                if (Networking.isNetworkAvailable(InviteActivity.this)) {

            //getMyConnections();

        } else {
            showSnackBar(getResources().getString(R.string.no_connection));
            showDialog_singleButton(getResources().getString(R.string.no_connection));
        }


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String message = bundle.getString("message");
            if (message.equals("imbyou")) {

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            }
        } else {

            if (getSupportActionBar() != null) {

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                mSkipTextView.setVisibility(View.GONE);

            }


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

    private void searchConnections(String value) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users");
        Query postsQuery = myRef.orderByChild("username").startAt(value).endAt(value + "\uF8FF");

        mAdapter = new FirebaseRecyclerAdapter<UserModel, InviteViewHolder>(UserModel.class,
                R.layout.item_invite,
                InviteViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(InviteViewHolder viewHolder, final UserModel model, int position) {
                final DatabaseReference postRef = getRef(position);
                final String friend_key = postRef.getKey();

                viewHolder.bindToPost(InviteActivity.this, model, friend_key,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Toast.makeText(InviteActivity.this, "sent success",
                                        Toast.LENGTH_SHORT).show();

                                String myKey = new Preferences().getUserKey(InviteActivity.this);

                                //Save all sent request to your account
                                InvitePeopleModel inviteModel = new InvitePeopleModel(model.username,
                                        model.email, "inviteSent", false, false, false, "");
                                DatabaseReference my_iRef = database.getReference("sent_request");
                                my_iRef.child(myKey).child(friend_key).setValue(inviteModel);

                                //Save request to your friend account
                                InvitePeopleModel InvitePeopleModel = new InvitePeopleModel(
                                        new Preferences().getUserName(InviteActivity.this),
                                        new Preferences().getEmail(InviteActivity.this),
                                        "invitationReceived", false, false, false, "");
                                DatabaseReference myRef = database.getReference("notification");
                                myRef.child(friend_key).child(myKey).setValue(InvitePeopleModel);

                            }
                        });
            }
        };

        mRecycler.setAdapter(mAdapter);

        // mRecycler.getRecycledViewPool().clear();


        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invite, menu);

        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

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
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.skip_textView:

                startActivity(new Intent(InviteActivity.this, DashboardActivity.class));
                finish();

                break;

            case R.id.search_button:

                searchView.onActionViewExpanded();

                break;

            case R.id.contacts_button:

                startActivity(new Intent(InviteActivity.this, InvitePhoneContactsActivity.class));

                break;

            default:
                break;
        }
    }

    @Override
    public boolean onClose() {
        mSearchViewRelativeLayout.setVisibility(View.VISIBLE);
        mRecycler.setVisibility(View.GONE);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {

        if (Networking.isNetworkAvailable(InviteActivity.this)) {

            searchConnections(s);
        } else {
            showSnackBar(getResources().getString(R.string.no_connection));
            showDialog_singleButton(getResources().getString(R.string.no_connection));
        }

        if (TextUtils.isEmpty(s)) {
            mRecycler.setVisibility(View.GONE);
        } else {
            mSearchViewRelativeLayout.setVisibility(View.GONE);
            mRecycler.setVisibility(View.VISIBLE);

            searchConnections(s);
        }


        return false;
    }
}
