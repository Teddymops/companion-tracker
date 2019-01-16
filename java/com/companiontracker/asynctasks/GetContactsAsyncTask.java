package com.companiontracker.asynctasks;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.companiontracker.interfaces.GetContactsInterface;
import com.companiontracker.model.UserContactModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class GetContactsAsyncTask extends AsyncTask<String, Integer, ArrayList<UserContactModel>> {

    public GetContactsInterface delegate;



    private Context context;

    public GetContactsAsyncTask(Context context) {
        this.context = context;
    }


    @Override
    protected void onPostExecute(ArrayList<UserContactModel> userContactModels) {
        super.onPostExecute(userContactModels);
        delegate.onGetContactsPostExecute(userContactModels);
    }

    @Override
    protected ArrayList<UserContactModel> doInBackground(String... params) {
        return getResponse();
    }

    ArrayList<UserContactModel> contactModelArrayList = new ArrayList<>();
    private ArrayList<UserContactModel> getResponse() {

        try {
            Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                    new String[]{ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                            ContactsContract.Contacts.PHOTO_URI,
                            ContactsContract.Contacts.HAS_PHONE_NUMBER,
                            ContactsContract.Contacts.STARRED}, null, null,
                    "upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");

            if (cursor != null) {

                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        if (Integer.valueOf(cursor.getString(cursor.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            String id = cursor.getString(cursor.getColumnIndex(
                                    ContactsContract.Contacts._ID));
                            String name = cursor.getString(cursor.getColumnIndex(
                                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                            String photo = cursor.getString(cursor.getColumnIndex(
                                    ContactsContract.Contacts.PHOTO_URI));
                            int isStarred = cursor.getInt(cursor.getColumnIndex(
                                    ContactsContract.Contacts.STARRED));

                            getPhoneNumber(id, name, photo, isStarred);

                        }

                    }
                }

                cursor.close();
            }
        } catch (Exception ignore) {
        }

        return contactModelArrayList;
    }

    private void getPhoneNumber(String id, String name, String photo, int isStarred) {
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = '" + id + "'", null, null);

        if (cursor != null) {

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    String phone = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));

                    if (!TextUtils.isEmpty(phone)) {
                        UserContactModel contactModel = new UserContactModel();

                        contactModel.setId(id);
                        contactModel.setName(name);
                        contactModel.setPhone(phone);
                        contactModel.setPhoto(photo);
                        contactModel.setIsStaredContact(isStarred);

                        this.contactModelArrayList.add(contactModel);


                    }

                }
            }

            cursor.close();
        }

    }
}
