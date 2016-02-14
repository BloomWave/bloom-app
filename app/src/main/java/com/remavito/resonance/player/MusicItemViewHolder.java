package com.remavito.resonance.player;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.remavito.resonance.MainActivity;
import com.remavito.resonance.R;

import java.util.ArrayList;

/**
 * Created by tom on 09/08/15.
 */
public class MusicItemViewHolder {


    TextView mTitleView;
    TextView mArtistView;
    ImageView mImage;
    static final int STATE_INVALID = -1;
    static final int STATE_PLAYABLE = 0;
    static final int STATE_PLAYING = 1;
    static final int STATE_PAUSED = 2;


    static View setupView(Activity activity, View convertView, ViewGroup parent,
                          MusicDescription description, int state) {

        MusicItemViewHolder holder;


        if (convertView == null) {
            convertView = LayoutInflater.from(activity)
                    .inflate(R.layout.music_list_item, parent, false);
            holder = new MusicItemViewHolder();
            holder.mTitleView = (TextView) convertView.findViewById(R.id.name);
            holder.mArtistView = (TextView) convertView.findViewById(R.id.artist);
            holder.mImage = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (MusicItemViewHolder) convertView.getTag();
        }

        holder.mTitleView.setText(description.getName());
        //holder.mArtistView.setText(description.getArtist()+ " _ playable : " +
         //                          String.valueOf(description.isPlayable()));
        if(description.isPlayable()){
            holder.mImage.setBackgroundColor(0xFF0000FF);
        }
        if(state == MusicItemViewHolder.STATE_PLAYING){
            holder.mImage.setBackgroundColor(0xFF00FF00);
        }
        if(state == MusicItemViewHolder.STATE_PAUSED){
            holder.mImage.setBackgroundColor(0xFFFF0000);
        }

        return convertView;
    }
}

class BrowseAdapter extends ArrayAdapter<MusicDescription> {
    private final String TAG = "Adapter Pymp";

    public BrowseAdapter(Activity context) {
        super(context, R.layout.music_list_item, new ArrayList<MusicDescription>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MusicDescription item = getItem(position);

        int itemState = MusicItemViewHolder.STATE_INVALID;
//        if (item.isPlayable()) {
//            itemState = MusicItemViewHolder.STATE_PLAYABLE;
//            Player player = ((MainActivity) getContext()).getAudio_player();
//            if (player != null) {
//                String currentPlaying = player.getFname();
//                Log.d(TAG, currentPlaying + " == " + item.getName());
//                if (currentPlaying == item.getName()) {
//                    int pbState = player.getPlayerState();
//                    if (pbState == Player.PLAYER_INVALID) {
//                        itemState = MusicItemViewHolder.STATE_INVALID;
//                    } else if (pbState == Player.PLAYER_RUN) {
//                        itemState = MusicItemViewHolder.STATE_PLAYING;
//                    } else {
//                        itemState = MusicItemViewHolder.STATE_PAUSED;
//                    }
//                }
//            }
//        }
        return MusicItemViewHolder.setupView((Activity) getContext(), convertView, parent,
                item, itemState);
    }
}