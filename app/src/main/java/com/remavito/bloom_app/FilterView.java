package com.remavito.bloom_app;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.remavito.bloom_app.player.Player;

/**
 * Created by tomMoral on 06/11/15.
 *
 */
public class FilterView {


    TextView freqText, qualText, gainText;
    SeekBar freqSlider, qualSlider, gainSlider;
    Switch chanSwitch;
    Spinner typeCombo;

    final private static int MIN_FREQ = 100;
    final private static int MAX_FREQ = 4000;
    final private static int BASE_FREQ = 700;
    final private static int SLIDER_PREC = 1000;

    final public static int FILTER_LOWPASS = 0;
    final public static int FILTER_LOWSHELF = 1;
    final public static int FILTER_HIGHPASS = 2;
    final public static int FILTER_HIGHSHELF = 3;
    final public static int FILTER_CHANNEL_LOW = 0;
    final public static int FILTER_CHANNEL_HIGH = 1;

    final private static String TAG = "FilterView";

    private int Fs, position;
    volatile public AudioFilter filter = new AudioFilter();
    private Player player;


    static View setupView(Activity activity, View convertView, ViewGroup parent, int _position) {

        final FilterView holder;



        if (convertView == null) {
            convertView = LayoutInflater.from(activity)
                    .inflate(R.layout.filter_view, parent, false);
            MainActivity main = (MainActivity) activity;

            holder = new FilterView();
            holder.position = _position;
            holder.player = main.player;

            // Get UI element for the holder
            holder.chanSwitch = (Switch) convertView.findViewById(R.id.chanSwitch);
            holder.typeCombo = (Spinner) convertView.findViewById(R.id.typeCombo);

            holder.freqText = (TextView) convertView.findViewById(R.id.freqText);
            holder.gainText = (TextView) convertView.findViewById(R.id.gainText);
            holder.qualText = (TextView) convertView.findViewById(R.id.qualText);
            holder.freqSlider = (SeekBar) convertView.findViewById(R.id.freqSlider);
            holder.freqSlider.setMax(MAX_FREQ);
            holder.gainSlider = (SeekBar) convertView.findViewById(R.id.gainSlider);
            holder.gainSlider.setMax(SLIDER_PREC);
            holder.qualSlider = (SeekBar) convertView.findViewById(R.id.qualSlider);
            holder.qualSlider.setMax(SLIDER_PREC);

            SeekBar.OnSeekBarChangeListener changeListener = new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    holder.set_frequency();
                }
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}
            };

            holder.freqSlider.setOnSeekBarChangeListener(changeListener);
            holder.gainSlider.setOnSeekBarChangeListener(changeListener);
            holder.qualSlider.setOnSeekBarChangeListener(changeListener);
            holder.freqSlider.setProgress(BASE_FREQ);
            holder.gainSlider.setProgress(SLIDER_PREC);
            holder.qualSlider.setProgress((int) (.8*SLIDER_PREC));

            holder.chanSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    holder.set_frequency();
                }
            });
            holder.typeCombo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    holder.set_frequency();
                }
                public void onNothingSelected(AdapterView<?> adapterView) {
                    holder.set_frequency();
                }
            });
            holder.set_frequency();


            convertView.setTag(holder);
        } else
            holder = (FilterView) convertView.getTag();

        return convertView;
    }

    public void set_frequency() {
        // Get Parameters and set text accordingly
        //Freq
        int Fc = this.freqSlider.getProgress();
        if (Fc < MIN_FREQ) {
            freqSlider.setProgress(MIN_FREQ);
            return;
        }
        freqText.setText(String.format("Freq coup : %6d", Fc));

        // Quality factor
        double Q = qualSlider.getProgress() * 1. / SLIDER_PREC;
        qualText.setText(String.format("Factor Q : %-6.2f", Q));

        // Gain
        double peakGain = gainSlider.getProgress() * 1. / SLIDER_PREC;
        gainText.setText(String.format("Peak Gain : %-6.2f", peakGain));

        int channel = FILTER_CHANNEL_LOW;
        int type = (int) typeCombo.getSelectedItemId();
        Log.d(TAG, String.format("Type : %d", type));
        //int type = FILTER_LOWPASS;
        this.chanSwitch.setText(String.format("%15s", "AI output"));
        if(this.chanSwitch.isChecked()){
            channel = FILTER_CHANNEL_HIGH;
            this.chanSwitch.setText(String.format("%15s", "Speaker output"));
        }

        //Change the frequency
        Log.d(TAG, "Freq changed to : " + String.valueOf(Fc));
        //filter.set_params(Fc, Q, peakGain, channel, type);
        player.set_params(position, Fc, Q, peakGain, channel, type);
    }

    public void apply(byte[] data, int i_seg, int l_seg){
        if(this.filter != null){
            filter.apply(data, i_seg, l_seg);
            Log.d(TAG, filter.getParam());
        }
    }
}
