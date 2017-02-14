package com.achyut.simplemusicplayer;

import android.content.Context;
import android.database.CursorJoiner;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;

/**
 * Created by bhand on 2/11/2017.
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicHolder> {
    ArrayList<MusicDetail> musicList;
    Context context;
    private MusicItemClickListener musicItemClickListener;

    public MusicAdapter(ArrayList<MusicDetail> musicList, Context context) {
        this.musicList = musicList;
        this.context = context;
    }

    public void setMusicItemClickListener(MusicItemClickListener musicItem) {
        this.musicItemClickListener = musicItem;
    }


    @Override
    public MusicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_list, parent, false);
        MusicHolder musicHolder = new MusicHolder(rootView);
        return musicHolder;
    }

    @Override
    public void onBindViewHolder(MusicHolder holder, final int position) {
        holder.musicName.setText(musicList.get(position).getMusicTitle());
        holder.artistName.setText(musicList.get(position).getArtistName());
        holder.musicLayout.setTag(position);
       /* holder.musicLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicItemClickListener != null) {
                    musicItemClickListener.onClick(musicList.get(position));
                }
            }
        });*/

    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }


    public static class MusicHolder extends RecyclerView.ViewHolder {
        TextView musicName, artistName;
        LinearLayout musicLayout;

        public MusicHolder(View itemView) {
            super(itemView);
            musicName = (TextView) itemView.findViewById(R.id.tv_name);
            artistName = (TextView) itemView.findViewById(R.id.tv_artist);
            musicLayout = (LinearLayout) itemView.findViewById(R.id.music_layout);
        }
    }

    public interface MusicItemClickListener {
        void onClick(MusicDetail result);
    }

}
