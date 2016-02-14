package com.remavito.resonance;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.remavito.resonance.player.MusicDescription;
import com.remavito.resonance.player.Player;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "FragmentHolder";

    private MediaRecorder rec;
    private boolean recording;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int section_number = this.getArguments().getInt(ARG_SECTION_NUMBER);
        if(section_number==1){
            View rootView = inflater.inflate(R.layout.fragment_player, container, false);
            ListView lv = (ListView) rootView.findViewById(R.id.songListView);
            MainActivity main = (MainActivity) getContext();
            lv.setAdapter(main.getSongAdapter());
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                      MusicDescription mu = (MusicDescription) parent.getItemAtPosition(position);
                      Log.d(TAG, "Click on " + mu.getName());
                      if (mu.isPlayable()) {
                          Log.d(TAG, "setSong()");
                          MainActivity main = (MainActivity) getContext();
                          main.player.setSong(mu.getName());
                      }
                  }
              }
            );

            return rootView;
        }
        else if(section_number == 2){
            return FilterFragment.getFilterFragment(inflater, container,
                    (MainActivity) getContext());
        }
        else if(section_number == 3){
            View rootView = inflater.inflate(R.layout.fragment_mesure, container, false);
            Button rec_btn = (Button) rootView.findViewById(R.id.rec_btn);
            final EditText fname = (EditText) rootView.findViewById(R.id.fname_text);
            MainActivity main = (MainActivity) getContext();
            recording = false;

            rec_btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!recording) {
                        rec = new MediaRecorder();
                        rec.setAudioSource(MediaRecorder.AudioSource.MIC);
                        rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        rec.setOutputFile(Player.MUSIC_DIR.getPath() + '/' + fname.getText());
                        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                        try {
                            rec.prepare();
                        } catch (java.io.IOException e) {
                            Log.e("RECORDER", "prepare() failed");
                            Log.e("RECORDER", e.getMessage());
                        }


                        rec.start();
                        recording = true;
                    } else {
                        recording = false;
                        rec.stop();
                        rec = null;
                    }
                }
            });


            return rootView;
        }
        View rootView = inflater.inflate(R.layout.fragment_holder, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText(getString(R.string.section_format, section_number));
        return rootView;
    }
}