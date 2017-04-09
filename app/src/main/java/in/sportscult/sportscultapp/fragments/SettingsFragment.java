package in.sportscult.sportscultapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import in.sportscult.sportscultapp.R;

import static in.sportscult.sportscultapp.MainDrawer.SETTINGS;

public class SettingsFragment extends Fragment {

    Switch live_match, live_score;


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);

        live_match = (Switch) view.findViewById(R.id.live_match);
        live_score = (Switch) view.findViewById(R.id.live_score);

        live_match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPreferences(getString(R.string.live_match), live_match.isChecked());
            }
        });

        live_score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPreferences(getString(R.string.live_score), live_score.isChecked());
            }
        });

        loadPreferences();

        return view;
    }

    private void setPreferences(String sw, boolean checked) {
        SharedPreferences sp = getActivity().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(sw,checked);
        editor.apply();

    }

    private void loadPreferences() {
        SharedPreferences sp = getActivity().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE );
        boolean lm = sp.getBoolean(getString(R.string.live_match), true);
        boolean ls = sp.getBoolean(getString(R.string.live_score), true);

        if(lm) live_match.setChecked(true);
        else live_match.setChecked(false);

        if(ls) live_score.setChecked(true);
        else live_score.setChecked(false);
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
