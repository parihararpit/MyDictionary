package com.arpitsingh.mydictionary.adapters;


import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arpitsingh.mydictionary.R;
import com.arpitsingh.mydictionary.data.DictContract;

/**
 * Created by ARPIT SINGH
 * 14/10/19
 */

public class CursorRecyclerAdapter extends RecyclerView.Adapter<CursorRecyclerAdapter.HistoryViewHolder> {
    private Cursor mCursor;
    private OnListItemClickListener mClickListener;
    private int mActionDrawableId;
    private int mSearchIconDrawableId;

    public interface OnListItemClickListener {
        public void onClick(String wordId);
    }

    public CursorRecyclerAdapter(Cursor cursor) {
        this.mCursor = cursor;
    }

    public CursorRecyclerAdapter(OnListItemClickListener clickListener, int seachIcon, int actionImageDraw) {
        this.mClickListener = clickListener;
        this.mActionDrawableId = actionImageDraw;
        this.mSearchIconDrawableId = seachIcon;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_items, parent, false);
        return new HistoryViewHolder(view);
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            Log.d("Cursor Adapter ", "Cursor is closed");
            mCursor.close();
        }
        mCursor = cursor;
        if (mCursor != null)
            notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        if (mCursor == null || !mCursor.moveToPosition(position)) {
            return;
        }
        holder.historyTextView.setText(mCursor.getString(mCursor.getColumnIndex(DictContract.DictEntry.WORD)));
        holder.itemView.setTag(mCursor.getString(mCursor.getColumnIndex(DictContract.DictEntry._ID)));

        if (mActionDrawableId != -1) holder.historyAction.setImageResource(mActionDrawableId);
        else holder.historyAction.setVisibility(View.INVISIBLE);
        if (mSearchIconDrawableId != -1)
            holder.historySearchIcon.setImageResource(mSearchIconDrawableId);
        else holder.historyAction.setVisibility(View.INVISIBLE);

    }

    @Override
    public int getItemCount() {
        if (mCursor != null && !mCursor.isClosed())
            return mCursor.getCount();
        return 0;
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView historyTextView;
        ImageView historyAction;
        ImageView historySearchIcon;

        private HistoryViewHolder(View itemView) {
            super(itemView);
            historyTextView = itemView.findViewById(R.id.search_items_word);
            historyAction = itemView.findViewById(R.id.search_items_action);
            historySearchIcon = itemView.findViewById(R.id.search_items_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            TextView tv = view.findViewById(R.id.search_items_word);
            mClickListener.onClick(tv.getText().toString().trim().toLowerCase());
        }
    }
}
