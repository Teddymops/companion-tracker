package com.companiontracker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.companiontracker.utility.Networking;
import com.companiontracker.utility.Preferences;

public class ChangePasswordActivity extends BaseActivity {

    private TextView mToolbarTitleTextView;
    private EditText mOldPasswordEditText;
    private EditText mNewPasswordEditText;
    private EditText mConfirmPasswordEditText;

    private FirebaseDatabase database;
    private static String LOGIN_DETAILS_REF = "login_details";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        configureToolbar();
        database = FirebaseDatabase.getInstance();

        mOldPasswordEditText = (EditText) findViewById(R.id.old_password_editText);
        mNewPasswordEditText = (EditText) findViewById(R.id.new_password_editText);
        mConfirmPasswordEditText = (EditText) findViewById(R.id.confirm_password_editText);


    }

    private void configureToolbar() {
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mainToolbar != null) {
            mToolbarTitleTextView = (TextView) mainToolbar.findViewById(R.id.toolbar_title_textView);
            mToolbarTitleTextView.setText(getString(R.string.change_password));

            setSupportActionBar(mainToolbar);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
    }

    private void isCorrectPassword(final String oldPassword, final String newPassword) {

                final DatabaseReference myRef = database.getReference(LOGIN_DETAILS_REF);
        myRef.child(new Preferences().getUserName(this) + "," + oldPassword)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.exists()) {
                            String userId = dataSnapshot.getValue().toString();

                             if (new Preferences().getUserKey(ChangePasswordActivity.this).equals(userId)) {

                                 myRef.child(new Preferences().getUserName(ChangePasswordActivity.this) + "," + newPassword).
                                        setValue(new Preferences().getUserKey(ChangePasswordActivity.this))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {


                                        myRef.child(new Preferences().getUserName(ChangePasswordActivity.this) + "," + oldPassword).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //TODO Go back activity
                                                hideProgressDialog();
                                                Toast.makeText(ChangePasswordActivity.this,"Password changed successfully",Toast.LENGTH_SHORT).show();
                                                finish();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //TODO

                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        hideProgressDialog();
                                        Toast.makeText(ChangePasswordActivity.this,"failed",Toast.LENGTH_SHORT).show();

                                    }
                                });
                            } else {
                                //Password did not match with userKey
                                hideProgressDialog();
                            }

                        } else {

                            mOldPasswordEditText.setError(getString(R.string.error_incorrect_password));
                            mOldPasswordEditText.requestFocus();

                            hideProgressDialog();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        showDialog_singleButton(databaseError.toException().toString());
                        hideProgressDialog();

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_done:
                if (Networking.isNetworkAvailable(ChangePasswordActivity.this)) {
                    savePassword();
                } else {
                    showSnackBar(getResources().getString(R.string.no_connection));
                    showDialog_singleButton(getResources().getString(R.string.no_connection));
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void savePassword() {

        String oldPassword = mOldPasswordEditText.getText().toString();
        String newPassword = mNewPasswordEditText.getText().toString();
        String confirmPassword = mConfirmPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(oldPassword)) {

            focusView = mOldPasswordEditText;
            cancel = true;

            mOldPasswordEditText.setError(getString(R.string.error_field_required));

        } else if (oldPassword.length() < 6) {

            focusView = mOldPasswordEditText;
            cancel = true;

            mOldPasswordEditText.setError(getString(R.string.error_invalid_password));

        } else if (TextUtils.isEmpty(newPassword)) {

            focusView = mNewPasswordEditText;
            cancel = true;

            mNewPasswordEditText.setError(getString(R.string.error_field_required));

        } else if (newPassword.length() < 6) {

            focusView = mNewPasswordEditText;
            cancel = true;

            mNewPasswordEditText.setError(getString(R.string.error_invalid_password));

        } else if (TextUtils.isEmpty(confirmPassword)) {


            focusView = mConfirmPasswordEditText;
            cancel = true;

            mConfirmPasswordEditText.setError(getString(R.string.error_field_required));

        } else {

            hideSoftKeyboard(mOldPasswordEditText,this);
            hideSoftKeyboard(mNewPasswordEditText,this);
            hideSoftKeyboard(mConfirmPasswordEditText,this);

            showProgressDialog();

            isCorrectPassword(oldPassword, newPassword);
        }

        if (cancel) {
            focusView.requestFocus();
        }



    }
}
