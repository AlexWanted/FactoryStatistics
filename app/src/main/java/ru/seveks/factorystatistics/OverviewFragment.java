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

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class OverviewFragment extends Fragment {
    int weight_1 = 80, weight_2 = 10, weight_3 = 15;

    private OverviewPresenter presenter;
    GraphView graphView;
    ArrayList<Float> values;

    public OverviewFragment() { }

    public static OverviewFragment newInstance() {
        OverviewFragment fragment = new OverviewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() != null) {
            if(getActivity().getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT)
                ((MainActivity) getActivity()).setStatusBarTranslucent(true, Color.WHITE);
            else
                ((MainActivity) getActivity()).setStatusBarTranslucent(false, Color.WHITE);
        }
        values = new ArrayList<>();
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

        graphView = view.findViewById(R.id.graph);
        presenter.getFilesInDirectory();

        graphView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
            if(getActivity().getSupportFragmentManager().findFragmentByTag("charts") == null) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment hoursFragment = HoursFragment.newInstance(values);
                ft.addToBackStack("charts");
                ft.hide(getActivity().getSupportFragmentManager().findFragmentByTag("overview"));
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
        return view;
    }

    public void updateChart(ArrayList<Float> values) {
        if (graphView != null) {
            this.values.clear();
            this.values.addAll(values);
            graphView.setBarValues(this.values, true);
        }
    }

    private void animateValues(final int value, final TextView number_text) {
        ValueAnimator animator = ValueAnimator.ofInt(0, value);
        animator.setDuration(950);
        animator.setStartDelay(100);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if(getContext() != null)
                    number_text.setText(getContext().getResources().getString(R.string.tonne,
                            Integer.parseInt(animation.getAnimatedValue().toString())));
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
