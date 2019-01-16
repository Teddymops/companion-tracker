package com.companiontracker.asynctasks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;


import com.companiontracker.interfaces.GetInviteContactList;
import com.companiontracker.model.FollowModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class GetInviteListTask extends AsyncTask<ArrayList<String>, Integer,
        ArrayList<FollowModel>> {


    private Context mContext;


    private String mSearchInvite = "";


    public GetInviteContactList delegate;

    public GetInviteListTask(Context context) {
        this.mContext = context;
    }

    public GetInviteListTask(Context context, String searchInvite) {
        this.mContext = context;
        this.mSearchInvite = searchInvite;
    }

    @Override
    protected ArrayList<FollowModel> doInBackground(ArrayList<String>... params) {
        return getInviteContacts();
    }

    @Override
    protected void onPostExecute(ArrayList<FollowModel> inviteModels) {
        super.onPostExecute(inviteModels);

        delegate.contactsToInvite(inviteModels, mSearchInvite.length() > 0);

    }

    private ArrayList<FollowModel> getInviteContacts() {
        ArrayList<FollowModel> inviteArray = new ArrayList<>();


        if (inviteArray.size() > 0) {
            Collections.sort(inviteArray, new Comparator<FollowModel>() {
                @Override
                public int compare(FollowModel one, FollowModel two) {
                    return one.getName().compareTo(two.getName());
                }
            });
        }

        return inviteArray;

    }

    private String formQuery() {

        return "";

    }

}
