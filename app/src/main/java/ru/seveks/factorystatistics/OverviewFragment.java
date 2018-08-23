package ru.seveks.factorystatistics;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import ru.seveks.factorystatistics.Overview.OverviewModel;
import ru.seveks.factorystatistics.Overview.OverviewPresenter;

public class OverviewFragment extends Fragment {
    int weight_1 = 80, weight_2 = 10, weight_3 = 15;

    private OverviewPresenter presenter;

    public OverviewFragment() { }

    public static OverviewFragment newInstance() {
        OverviewFragment fragment = new OverviewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() != null)
            ((MainActivity)getActivity()).setStatusBarTranslucent(true, Color.WHITE);

        OverviewModel model = new OverviewModel();
        presenter = new OverviewPresenter(model);
        presenter.attachFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_overview, container, false);

        animateValues(weight_1, (TextView)view.findViewById(R.id.number_by_day));
        animateValues(weight_2, (TextView)view.findViewById(R.id.number_by_working_day));
        animateValues(weight_3, (TextView)view.findViewById(R.id.number_by_previous_working_day));

        String full_day = getContext().getResources().getQuantityString(R.plurals.weight, weight_1)+" "
                        + getContext().getResources().getString(R.string.full_day);
        ((TextView) view.findViewById(R.id.by_day)).setText(full_day);

        String this_day = getContext().getResources().getQuantityString(R.plurals.weight, weight_2)+" "
                        + getContext().getResources().getString(R.string.this_day);
        ((TextView) view.findViewById(R.id.by_working_day)).setText(this_day);

        String previous_day = getContext().getResources().getQuantityString(R.plurals.weight, weight_3)+" "
                            + getContext().getResources().getString(R.string.previous_day);
        ((TextView) view.findViewById(R.id.by_previous_working_day)).setText(previous_day);

        final GraphView graphView = view.findViewById(R.id.graph);
        final ArrayList<Float> values = new ArrayList<>();
        for (int i=0; i<24; i++){
            values.add(new Random().nextFloat() % 136);
        }
        graphView.setBarValues(values, true);

        graphView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
            if(getActivity().getSupportFragmentManager().findFragmentByTag("charts") == null) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment hoursFragment = HoursFragment.newInstance(values);
                ft.addToBackStack("charts");
                ft.add(R.id.fragments_container, hoursFragment, "charts");
                ft.commit();
            }
            }
        });

        view.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if(getActivity().getSupportFragmentManager().findFragmentByTag("settings") == null) {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment settingsFragment = new SettingsFragment();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        settingsFragment.setEnterTransition(TransitionInflater.from(getContext())
                                .inflateTransition(R.transition.slide_in));
                    }

                    ft.addToBackStack("settings");
                    ft.add(R.id.fragments_container, settingsFragment, "settings");
                    ft.commit();
                }
            }
        });

        presenter.getFilesInDirectory();
        return view;
    }

    private void animateValues(final int value, final TextView number_text) {
        ValueAnimator animator = ValueAnimator.ofInt(0, value);
        animator.setDuration(1250);
        animator.setStartDelay(100);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if(getContext() != null)
                    number_text.setText(animation.getAnimatedValue().toString());
            }
        });
        animator.start();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter.detachFragment();
    }
}
