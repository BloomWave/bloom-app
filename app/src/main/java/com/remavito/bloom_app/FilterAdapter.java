package com.remavito.bloom_app;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by tomMoral on 08/11/15.
 */

public class FilterAdapter extends ArrayAdapter<FilterView> {
    private final String TAG = "Adapter Pymp";

    public FilterAdapter(Activity context) {
        super(context, R.layout.filter_view, new ArrayList<FilterView>());
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        FilterView item = getItem(position);
        return FilterView.setupView((Activity) getContext(), convertView, parent,
                                    position);
    }
}


