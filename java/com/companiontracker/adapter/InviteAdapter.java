package com.companiontracker.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.companiontracker.R;
import com.companiontracker.interfaces.InviteUserAdapterInterface;
import com.companiontracker.model.FollowModel;
import com.companiontracker.model.UserContactModel;
import com.companiontracker.utility.ShowImage;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;


public class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.ViewHolder> implements
        FastScrollRecyclerView.SectionedAdapter {

    private Context mContext;

    private ArrayList<UserContactModel> mInviteArray;

    public InviteUserAdapterInterface delegate;

    public InviteAdapter(Context context, ArrayList<UserContactModel> data) {
        this.mContext = context;
        this.mInviteArray = data;

    }

    public void updateInviteList(ArrayList<UserContactModel> data) {
        this.mInviteArray = data;

        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView mUserPicImageView;
        TextView mUserNameTextView;
        TextView mPhoneTextView;
        Button mInviteButton;

        public ViewHolder(View v) {
            super(v);

            mUserPicImageView = (ImageView) v.findViewById(R.id.user_pic_imageView);
            mUserNameTextView = (TextView) v.findViewById(R.id.user_name_textView);
            mPhoneTextView = (TextView) v.findViewById(R.id.phone_textView);
            mInviteButton = (Button) v.findViewById(R.id.invite_button);

        }

    }

    @Override
    public InviteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reg_r_invite, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        ShowImage.getInstance().showPhoto(mContext, mInviteArray.get(position).getPhoto(),
                mInviteArray.get(position).getName(), holder.mUserPicImageView);

        holder.mUserNameTextView.setText(mInviteArray.get(position).getName());
        holder.mPhoneTextView.setText(mInviteArray.get(position).getPhone());

        holder.mInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delegate.onInviteFriend(mInviteArray.get(holder.getAdapterPosition()));

            }
        });

    }

    @Override
    public int getItemCount() {
        return mInviteArray.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(mInviteArray.get(position).getName().charAt(0));
    }

}
