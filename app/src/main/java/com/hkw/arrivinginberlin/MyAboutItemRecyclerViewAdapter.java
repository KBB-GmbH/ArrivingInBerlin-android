package com.hkw.arrivinginberlin;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hkw.arrivinginberlin.AboutItemFragment.OnListFragmentInteractionListener;
import com.hkw.arrivinginberlin.aboutItem.AboutContent.AboutItem;

import java.util.List;


public class MyAboutItemRecyclerViewAdapter extends RecyclerView.Adapter<MyAboutItemRecyclerViewAdapter.ViewHolder> {

    private final List<AboutItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final Context mContext;

    public MyAboutItemRecyclerViewAdapter(List<AboutItem> items, OnListFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_aboutitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mContentView.setText(getTextItem(mValues.get(position).content));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public AboutItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    private String getTextItem(String itemName){
        switch (itemName){
            case "privacy":
                return mContext.getString(R.string.privacy_title);
            case "legal":
                return mContext.getString(R.string.legal_title);
            case "terms":
                return mContext.getString(R.string.terms_title);
            default:
                return mContext.getString(R.string.privacy_title);
        }
    }
}
