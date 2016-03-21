package com.remavito.bloom_app;

import android.app.Activity;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by tomMoral on 07/11/15.
 */
public class FilterFragment extends Fragment {

    final private static int MIN_FREQ = 100;
    final private static int MAX_FREQ = 4000;
    final private static int BASE_FREQ = 700;
    final private static int SLIDER_PREC = 1000;
    final public static int FILTER_LOWPASS = 0;
    final public static int FILTER_LOWSHELF = 1;


    private TextView freqText, lvolText, rvolText;
    private SeekBar freqSlider, lvolSlider, rvolSlider;
    private Switch typeSwitch;
    private MainActivity main;
    private Button addBtn;
    private ListView filters;
    private FilterAdapter filterList;

    public FilterFragment() {
    }

    static public View getFilterFragment(LayoutInflater inflater, ViewGroup container,
                                         MainActivity _main){

        final FilterFragment filterFragment = new FilterFragment();
        filterFragment.main = _main;

        View rootView = inflater.inflate(R.layout.fragment_filter, container, false);
        filterFragment.freqText = (TextView) rootView.findViewById(R.id.cutoff_freq_text);
        filterFragment.lvolText = (TextView) rootView.findViewById(R.id.lvol_text);
        filterFragment.rvolText = (TextView) rootView.findViewById(R.id.rvol_text);
        filterFragment.freqSlider = (SeekBar) rootView.findViewById(R.id.cutoff_freq_slider);
        filterFragment.rvolSlider = (SeekBar) rootView.findViewById(R.id.rvol_slider);
        filterFragment.lvolSlider = (SeekBar) rootView.findViewById(R.id.lvol_slider);
        filterFragment.typeSwitch = (Switch) rootView.findViewById(R.id.cutoff_type_switch);
        filterFragment.filters = (ListView) rootView.findViewById(R.id.filter_list_view);
        filterFragment.filterList = filterFragment.main.player.filterList;
        filterFragment.filters.setAdapter(filterFragment.main.player.filterList);

        filterFragment.addBtn = (Button) rootView.findViewById(R.id.addFilterBtn);
        filterFragment.addBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                filterFragment.main.player.addFilter();
            }
        });

        filterFragment.freqSlider.setMax(MAX_FREQ);
        filterFragment.freqSlider.setProgress(BASE_FREQ);
        filterFragment.freqSlider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        filterFragment.set_cutoff_freq();
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );
        filterFragment.rvolSlider.setMax(SLIDER_PREC);
        filterFragment.rvolSlider.setProgress(SLIDER_PREC);
        filterFragment.rvolSlider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        filterFragment.set_cutoff_freq();
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );
        filterFragment.lvolSlider.setMax(SLIDER_PREC);
        filterFragment.lvolSlider.setProgress(SLIDER_PREC);
        filterFragment.lvolSlider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        filterFragment.set_cutoff_freq();
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );
        filterFragment.typeSwitch.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        filterFragment.set_cutoff_freq();
                    }
                }
        );

        filterFragment.set_cutoff_freq();

        return rootView;

    }

    public void set_cutoff_freq(){
        int Fc = freqSlider.getProgress();
        if(Fc < MIN_FREQ){
            freqSlider.setProgress(MIN_FREQ);
            return;
        }
        freqText.setText(String.format("Freq coup : %6d", Fc));
        int type = FILTER_LOWPASS;
        typeSwitch.setText("LowPass");
        if(typeSwitch.isChecked()){
            type = FILTER_LOWSHELF;
            typeSwitch.setText("LowShelf");
        }
        double rvol = rvolSlider.getProgress() * 1. /SLIDER_PREC;
        rvolText.setText(String.format("Speakers  : %-6.2f", rvol));
        double lvol = lvolSlider.getProgress() * 1. /SLIDER_PREC;
        lvolText.setText(String.format("AI output : %-6.2f", lvol));
        main.player.set_cutoff_params(Fc, type, rvol, lvol);
    }
}
