package com.remavito.bloom_app;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.remavito.bloom_app.audio_processing.FFT_Processing;
import com.remavito.bloom_app.player.MusicDescription;
import com.remavito.bloom_app.player.Player;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Handler;


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
    private AudioRecord rec2;
    private boolean recording;
    private FFT_handler hft;

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

        final MainActivity main = (MainActivity) getContext();

        if(section_number==1){
            View rootView = inflater.inflate(R.layout.fragment_player, container, false);

            ListView lv = (ListView) rootView.findViewById(R.id.songListView);
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
            final GraphView graph = (GraphView) rootView.findViewById(R.id.graph);
            FloatingActionButton rec_btn = (FloatingActionButton) rootView.findViewById(R.id.rec_btn);
            recording = false;

            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                    new DataPoint(0, 1),
                    new DataPoint(Constants.FFT_SIZE/2, 1)
            });
            graph.addSeries(series);
            GridLabelRenderer grid_label_randerer = graph.getGridLabelRenderer();
            grid_label_randerer.setHorizontalLabelsVisible(false);
            grid_label_randerer.setVerticalLabelsVisible(false);
            graph.onDataChanged(true, true);
            graph.getViewport().setScalable(true);
            graph.getViewport().setScrollable(true);



            rec_btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!recording) {
                        int sample_freq = 44100;
                        int buff_size = AudioRecord.getMinBufferSize(sample_freq, AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT);
                        rec2 = new AudioRecord(MediaRecorder.AudioSource.MIC, sample_freq,
                                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buff_size);
                        Log.d("RECORDER", "buff size = " + buff_size);

                        rec2.startRecording();
                        hft = new FFT_handler(rec2, graph, main);
                        hft.start();
                        recording = true;
                    } else {
                        recording = false;
                        rec2.stop();
                        hft.stop_fft();
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


class FFT_handler extends Thread{
    private AudioRecord audio_src;
    private boolean state;
    private FFT_Processing pft;
    private GraphView graph;
    private Context context;

    public FFT_handler(AudioRecord rec, GraphView _graph, Context _context){
        audio_src = rec;
        state = false;
        pft = new FFT_Processing(Constants.FFT_SIZE);
        graph = _graph;
        context = _context;
    }

    @Override
    public void run() {
        state = true;
        ByteBuffer buff2 = ByteBuffer.allocateDirect(Constants.FFT_SIZE*2);

        double[] fft = new double[Constants.FFT_SIZE];
        while (state){
            buff2.clear();
            audio_src.read(buff2, Constants.FFT_SIZE * 2);//, AudioRecord.READ_BLOCKING);
            pft.rfft(buff2, fft);
            final LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
            for(int k=0; k < Constants.FFT_SIZE / 2; k++){
                series.appendData(new DataPoint(k, fft[k]), true, Constants.FFT_SIZE/2);
            }
            graph.post(new Runnable() {
                @Override
                public void run() {
                    graph.removeAllSeries();
                    graph.addSeries(series);
                    Log.d("Graph", "Add new fft");
                }
            });
        }


    }

    public void stop_fft(){
        Log.d("FFT-thread", "Stop!!!!");
        state = false;
    }
}
