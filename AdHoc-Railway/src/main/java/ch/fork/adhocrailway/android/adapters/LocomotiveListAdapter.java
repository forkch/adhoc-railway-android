package ch.fork.adhocrailway.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.android.R;
import ch.fork.adhocrailway.android.utils.ImageHelper;

/**
 * Created by fork on 4/16/14.
 */
public class LocomotiveListAdapter extends ArrayAdapter<Locomotive> {
    private final Context context;
    private final List<Locomotive> locomotives;

    public LocomotiveListAdapter(Context context, List<Locomotive> locomotives) {
        super(context, R.layout.locomotive_row, locomotives);
        this.context = context;
        this.locomotives = locomotives;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.locomotive_row, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        textView.setText(locomotives.get(position).getName());
        ImageHelper.fillImageViewFromBase64ImageString(imageView, locomotives.get(position).getImageBase64());

        return rowView;
    }

}
