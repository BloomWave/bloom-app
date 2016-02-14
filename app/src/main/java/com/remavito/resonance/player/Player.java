package com.remavito.resonance.player;

import android.app.Activity;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.*;
import android.os.Process;
import android.util.Log;
import android.widget.ListAdapter;

import com.remavito.resonance.AudioFilter;
import com.remavito.resonance.FilterAdapter;
import com.remavito.resonance.FilterView;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tom on 02/08/15.
 */
public class Player extends Thread {
    private WavInfo wi;
    private byte[] data;
    private byte[] data_processed;
    private InputStream soundtrack;
    private String fname;
    private int state, status, L, temp_song;


    private AudioTrack audio_track;
    private AudioManager audio_manager;
    private int bufferSize;

    private BrowseAdapter songListAdapter;
    public FilterAdapter filterList;
    private List<AudioFilter> filters;

    final private String TAG = "Player";
    private double[] A_low = {1.045750809331701, -1.991609855149324, 0.954249190668299};
    private double[] B_low = {0.002097536212669, 0.004195072425338, 0.002097536212669};
    private double[] A_high = {1.039229547863922, -1.993834667466256, 0.960770452136077};
    private double[] B_high = {0.998458666866564, -1.996917333733128, 0.998458666866564};
    private double volumR = 1.0;
    private double volumL = 1.0;
    private int Fs;

    // Player State
    final public static int PLAYER_INVALID = -1;
    final public static int PLAYER_STOP = 0;
    final public static int PLAYER_RUN = 1;
    final public  static int PLAYER_RESTART = 2;
    final public static int STATUS_STALED = 0;
    final public  static int STATUS_STARTED = 1;
    final public static int FILTER_LOWPASS = 0;
    final public static int FILTER_LOWSHELF = 1;


    final public static File MUSIC_DIR = new File(Environment.getExternalStorageDirectory(),
            "pymp");


    private short[] delayBuffer_low, delayBuffer_high;

    public Player(Activity context) {
        if (!MUSIC_DIR.exists()) {
            boolean res = MUSIC_DIR.mkdir();
            if (!res)
                Log.e(TAG, "couldn't create Pymp directory");
            else
                Log.i(TAG, "Successful in creating music dir");
        } else
            Log.d(TAG, "MUSIC_DIR already exists");

        songListAdapter = new BrowseAdapter(context);

        filterList = new FilterAdapter(context);
        filters = new ArrayList<AudioFilter>();
        getSongs();
        setSong(songListAdapter.getItem(0).getName());
        state = PLAYER_STOP;
        status = STATUS_STALED;


        bufferSize = AudioTrack.getMinBufferSize(
                wi.getRate(),
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        audio_manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audio_manager.setRouting(
                AudioManager.MODE_CURRENT,
                AudioManager.ROUTE_BLUETOOTH|AudioManager.ROUTE_SPEAKER,
                AudioManager.ROUTE_ALL
                );
        //audio_manager.setBluetoothA2dpOn(true);
        audio_track = new AudioTrack(AudioManager.STREAM_MUSIC, wi.getRate(),
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize, AudioTrack.MODE_STREAM);
        Log.d(TAG, "IS bluetooth on: " + audio_manager.isBluetoothA2dpOn());
        Log.d(TAG, "IS bluetooth on: " + audio_manager.isSpeakerphoneOn());
        Fs = wi.getRate();


        L = bufferSize / 2;

        delayBuffer_low = new short[L];
        delayBuffer_high = new short[L];
        reset();

    }


    // Get the .wav musics and set the songAdapter
    private void getSongs() {
        // get .wav files in MUSIC_DIR
        File[] songs = MUSIC_DIR.listFiles(new FilenameFilter(){
            public boolean accept(File file, String s) {
                return s.endsWith(".wav");
            }
        });

        // fill the adapter
        songListAdapter.clear();
        for(int i = 0; i < songs.length; i ++){
            File f = songs[i];
            Log.d(TAG, String.format("Add song %s", f.getName()));
            songListAdapter.add(new MusicDescription(f.getName(), String.valueOf(f.exists())));
        }
        songListAdapter.notifyDataSetChanged();
    }

    public void set_cutoff_params(int Fc, int type, double rvol, double lvol){
        //Change the filter parameters

        volumR = rvol;
        volumL = lvol;

        double K = Math.tan(Math.PI * Fc / Fs);
        double Q = 0.8;
        double peakGain = 0.0;

        A_low[0] = 1.0;
        A_high[0] = 1.0;
        switch (type) {
            case FILTER_LOWPASS: {
                double norm = 1 / (1 + K / Q + K * K);
                // Low pass filter adaptation
                B_low[0] = K * K * norm;
                B_low[1] = 2 * B_low[0];
                B_low[2] = B_low[0];
                A_low[1] = 2 * (K * K - 1) * norm;
                A_low[2] = (1 - K / Q + K * K) * norm;

                // high pass filter adaptation
                B_high[0] = 1 * norm;
                B_high[1] = -2 * B_high[0];
                B_high[2] = B_high[0];
                A_high[1] = 2 * (K * K - 1) * norm;
                A_high[2] = (1 - K / Q + K * K) * norm;
                break;
            }
            case FILTER_LOWSHELF: {
                double SQRT2 = Math.sqrt(2.0);
                double norm = 1 / (1 + SQRT2 * K + K * K);
                double V = Math.pow(10, Math.abs(peakGain) / 20);
                // Low pass filter
                B_low[0] = (1 + Math.sqrt(2 * V) * K + V * K * K) * norm;
                B_low[1] = 2 * (V * K * K - 1) * norm;
                B_low[2] = (1 - Math.sqrt(2 * V) * K + V * K * K) * norm;
                A_low[1] = 2 * (K * K - 1) * norm;
                A_low[2] = (1 - SQRT2 * K + K * K) * norm;
                // High pass filter
                norm = 1 / (1 + SQRT2 * K + K * K);
                B_high[0] = (V + Math.sqrt(2 * V) * K + K * K) * norm;
                B_high[1] = 2 * (K * K - V) * norm;
                B_high[2] = (V - Math.sqrt(2 * V) * K + K * K) * norm;
                A_high[1] = 2 * (K * K - 1) * norm;
                A_high[2] = (1 - SQRT2 * K + K * K) * norm;
                break;
            }
        }
    }
    public void set_volumR(double vol){
        //Change the frequency
        Log.d(TAG, "Volum changed to : " + String.valueOf(vol));
        volumR = vol;
    }
    public void set_volumL(double vol){
        //Change the frequency
        Log.d(TAG, "Volum changed to : " + String.valueOf(vol));
        volumL = vol;
    }

    public void pause_audio(){
        audio_track.flush();
        audio_track.pause();
        state = PLAYER_STOP;
    }

    public void play_audio(){
        audio_track.play();
        state = PLAYER_RUN;
    }

    public boolean isRunning(){
        return status == STATUS_STARTED;
    }


    private InputStream load(String fname) throws FileNotFoundException{
        File file = new File(MUSIC_DIR, fname);
        soundtrack = new FileInputStream(file);
        return soundtrack;
    }


    @Override
    public void run() {

        Log.d(TAG, "Start playing music");
        if(state == PLAYER_INVALID)
            return;
        status = STATUS_STARTED;
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        if(audio_track.getState() == AudioTrack.STATE_UNINITIALIZED)
            Log.d(TAG, "Uninitialized");
        int j = 0;
        int delayPos = 0;
        play_audio();
        while(temp_song < data_processed.length/L){
            if(audio_track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                audio_track.write(data_processed, temp_song * L, L);
                temp_song++;
                for(j = temp_song*L; j < (temp_song+1)*L; j+=4){
                    short xn = (short)(getSample(data, j) / 2);
                    xn += getSample(data, j+2)/2;
                    short yL = (short) (delayBuffer_low[delayPos] + B_low[0]*xn/A_low[0]);
                    short yH = (short) (delayBuffer_high[delayPos] + B_high[0]*xn/A_high[0]);
                    delayBuffer_low[delayPos] = yL;
                    delayBuffer_low[(delayPos+1)%L] += (B_low[1]*xn - A_low[1]*yL)/A_low[0];
                    delayBuffer_low[(delayPos+2)%L] += (B_low[2]*xn - A_low[2]*yL)/A_low[0];
                    delayBuffer_high[delayPos] = yH;
                    delayBuffer_high[(delayPos+1)%L] += (B_high[1]*xn - A_high[1]*yH)/A_high[0];
                    delayBuffer_high[(delayPos+2)%L] += (B_high[2]*xn - A_high[2]*yH)/A_high[0];
                    setSample(data_processed, j, (short) (volumL*delayBuffer_low[delayPos]));
                    setSample(data_processed, j+2, (short) (volumR*delayBuffer_high[delayPos]));
                    delayBuffer_low[delayPos] = 0;
                    delayBuffer_high[delayPos] = 0;
                    delayPos = (delayPos + 1) % L;
                }
                FilterView item;
                for (j = 0; j < filterList.getCount(); j++){
                    //item = filterList.getItem(j);
                    //item.apply(data_processed, temp_song, L);
                    filters.get(j).apply(data_processed, temp_song, L);
                    Log.d(TAG, filters.get(j).getParam());
                }

            }else if(state == PLAYER_RESTART) {
                reset();
                delayPos = 0;
                play_audio();

            }else{
                SystemClock.sleep(1000);
            }
        }
        Log.d(TAG, "End run!!");
        state = PLAYER_STOP;
        run();
    }

    private void reset(){
        temp_song = 0;
        for(int j = 0; j < L; j ++){
            delayBuffer_high[j] = 0;
            delayBuffer_low[j] = 0;
        }

    }


    private static short getSample(byte[] buffer, int position) {
        return (short) (((buffer[position + 1] & 0xff) << 8) | (buffer[position] & 0xff));
    }
    public static void setSample(byte[] buffer, int position, short sample) {
        buffer[position] = (byte) (sample & 0xff);
        buffer[position + 1] = (byte) ((sample >> 8) & 0xff);
        buffer[position + 2] = (byte) ((sample >> 16) & 0xff);
        buffer[position + 3] = (byte) ((sample >> 24) & 0xff);
    }

    public String getFname(){
        return fname;
    }

    public int getPlayerState(){
        return state;
    }

    public void setSong(String _fname){
        state = PLAYER_INVALID;
        fname = _fname;
        if(audio_track != null)
            audio_track.pause();
        try {
            load(_fname);
            wi = WavInfo.readHeader(soundtrack);
            data = WavInfo.readWavPcm(wi, soundtrack);
            data_processed = new byte[data.length];
        } catch (FileNotFoundException e){
            Log.e(TAG, "File not found, fail to initiate the player.");
            Log.d(TAG, _fname);
            return;
        } catch (DecoderException e) {
            e.printStackTrace();
            Log.e(TAG, "File not found, fail to initiate the player.");
            Log.d(TAG, _fname);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "File not found, fail to initiate the player.");
            Log.d(TAG, _fname);
            return;
        }

        state = PLAYER_RESTART;
        Fs = wi.getRate();

        FilterView item;
        for (int j = 0; j < filterList.getCount(); j++){
            item = filterList.getItem(j);
            item.filter.setFs(Fs);
        }

    }


    public void set_params(int position, int _Fc, double Q, double peakGain, int chan, int type){
        filters.get(position).set_params(_Fc, Q, peakGain, chan, type);
    }

    public ListAdapter getSongAdapter() {
        return this.songListAdapter;
    }

    public void addFilter(){
        filters.add(new AudioFilter());
        filterList.add(new FilterView());
        filterList.notifyDataSetChanged();
    }
}
