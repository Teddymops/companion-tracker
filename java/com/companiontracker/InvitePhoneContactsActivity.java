package com.companiontracker;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.companiontracker.adapter.InviteAdapter;
import com.companiontracker.asynctasks.GetContactsAsyncTask;
import com.companiontracker.asynctasks.GetInviteListTask;
import com.companiontracker.decoration.DividerDecoration;
import com.companiontracker.interfaces.GetContactsInterface;
import com.companiontracker.interfaces.InviteUserAdapterInterface;
import com.companiontracker.model.UserContactModel;
import com.companiontracker.utility.AsyncTaskTools;
import com.companiontracker.utility.Networking;

import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class InvitePhoneContactsActivity extends BaseActivity implements InviteUserAdapterInterface,
        SearchView.OnQueryTextListener, SearchView.OnCloseListener,
        GetContactsInterface {

    //Global mutable views
    private RecyclerView mInviteRecyclerView;
    private TextView mNoDataTextView;
    private ProgressBar mInviteProgressBar;

    //Holds invite adapter
    private InviteAdapter mInviteAdapter;

    //Holds the navigation flow, holds weather activity was opened from Profile
    private boolean isFromProfile = false;

    //Asynchronous task
    private GetInviteListTask mGetInviteListTask;
    private TextView mToolbarTitleTextView;

    private GetContactsAsyncTask asyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_r_invite);

        configureToolbar(mToolbarTitleTextView);

        mInviteRecyclerView = (RecyclerView) findViewById(R.id.invite_recyclerView);
        mNoDataTextView = (TextView) findViewById(R.id.no_data_textView);
        mInviteProgressBar = (ProgressBar) findViewById(R.id.invite_progressBar);

        //Changes progress bar color
        mInviteProgressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);

        mInviteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mInviteRecyclerView.addItemDecoration(new DividerDecoration(this));

        mInviteRecyclerView.setHasFixedSize(true);

        if (mToolbarTitleTextView != null) {
            //Setting name for toolbar
            mToolbarTitleTextView.setText(getString(R.string.contacts));
        }

        getContactsAsynTask();
        //getContacts();

    }

    private void getContactsAsynTask() {
        mInviteProgressBar.setVisibility(View.VISIBLE);
        if (Networking.isNetworkAvailable(this)) {
            asyncTask = new GetContactsAsyncTask(this);
            asyncTask.delegate = this;
            AsyncTaskTools.execute(asyncTask);
        } else {
            showDialog_singleButton(getResources().getString(R.string.no_connection));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invite, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

            switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendInvitation(String phone) {

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Here is the share content body";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My friend tracker");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));

    }

    //Holds contact data
    private ArrayList<UserContactModel> contactModelArrayList;


    public void contactsToInvite(ArrayList<UserContactModel> data) {
        mInviteProgressBar.setVisibility(View.GONE);

        if (data != null && data.size() > 0) {
            mNoDataTextView.setVisibility(View.GONE);
            mInviteRecyclerView.setVisibility(View.VISIBLE);

            if (mInviteAdapter == null) {
                mInviteAdapter = new InviteAdapter(this, data);
                mInviteAdapter.delegate = this;
                mInviteRecyclerView.setAdapter(mInviteAdapter);

            } else {
                mInviteAdapter.updateInviteList(data);

            }

        } else {
            //No records found, displays the message to user
            mNoDataTextView.setVisibility(View.VISIBLE);
            mInviteRecyclerView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onInviteFriend(UserContactModel selectedFriend) {
        String mSelectedInvitePhone = selectedFriend.getPhone();

        sendInvitation(selectedFriend.getPhone());

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Don't care about this.
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filterResults(newText);

        return true;
    }


    @Override
    public boolean onClose() {
        return false;
    }


    public void filterResults(String filterBy) {
        filterBy = filterBy.toLowerCase();

        ArrayList<UserContactModel> filteredContactModel = new ArrayList<>();

        for (int i = 0; i < contactModelArrayList.size(); i++) {
            if (contactModelArrayList.get(i).getName().toLowerCase().contains(filterBy)) {
                filteredContactModel.add(contactModelArrayList.get(i));
            }
        }

        contactsToInvite(filteredContactModel);
    }

    @Override
    public void onGetContactsPostExecute(ArrayList<UserContactModel> contacts) {
        contactModelArrayList = contacts;
        contactsToInvite(contacts);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            asyncTask.cancel(true);
        } catch (Exception e) {
            //Do Nothing
        }
    }
}
