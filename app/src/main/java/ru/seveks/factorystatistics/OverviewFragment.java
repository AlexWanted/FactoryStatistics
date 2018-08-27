package ru.seveks.factorystatistics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.transition.TransitionSet;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ru.seveks.factorystatistics.Overview.OverviewPresenter;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class OverviewFragment extends Fragment {

    private final String PRESENTER_KEY = "presenter";
    private OverviewPresenter presenter;
    GraphView graphView;
    SwipeRefreshLayout refreshLayout;
    TextView by_day, by_working_day, by_previous_working_day;
    double prevMaxBarValue = 0;
    public OverviewFragment() { }

    public static OverviewFragment newInstance() {
        return new OverviewFragment();
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
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_overview, container, false);

        by_day = view.findViewById(R.id.number_by_day);
        by_working_day = view.findViewById(R.id.number_by_working_day);
        by_previous_working_day = view.findViewById(R.id.number_by_previous_working_day);
        refreshLayout = view.findViewById(R.id.refresh);
        graphView = view.findViewById(R.id.graph);

        if (savedInstanceState != null) {
            presenter = savedInstanceState.getParcelable(PRESENTER_KEY);
            if (presenter != null && getContext() != null) {
                presenter.attachFragment(this);
                by_day.setText(getContext().getResources().getString(R.string.tonne, presenter.getWeight_1()));
                by_working_day.setText(getContext().getResources().getString(R.string.tonne, presenter.getWeight_2()));
                by_previous_working_day.setText(getContext().getResources().getString(R.string.tonne, presenter.getWeight_3()));
                if (graphView != null)
                    graphView.setBarValues(presenter.getValues(), false, false);
            }
        } else {
            presenter = new OverviewPresenter();
            presenter.attachFragment(this);
            presenter.getFilesInDirectory();
            refreshLayout.setRefreshing(true);
        }

        graphView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity().getSupportFragmentManager().findFragmentByTag("charts") == null) {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment hoursFragment = HoursFragment.newInstance(presenter.getValues());
                    ft.addToBackStack("charts");
                    ft.setCustomAnimations(R.anim.slide_in_to_top, R.anim.slide_out_to_bottom, R.anim.slide_in_to_top, R.anim.slide_out_to_bottom);
                    //ft.hide(getActivity().getSupportFragmentManager().findFragmentByTag("overview"));
                    ft.add(R.id.fragments_container, hoursFragment, "charts");
                    ft.commit();
                }
            }
        });

        view.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
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

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.getFilesInDirectory();
            }
        });
        return view;
    }

    public void onConnectionError() {
        Toast.makeText(getContext(), "Ошибка подключения", Toast.LENGTH_SHORT).show();
        refreshLayout.setRefreshing(false);

    }
    public void updateChart(ArrayList<Float> values, float weight_1, float weight_2, float weight_3) {
        if (graphView != null) {
            if (prevMaxBarValue <= 1) graphView.setBarValues(values, true, false);
            else graphView.setBarValues(values, true, true);
            prevMaxBarValue = graphView.getMaxBarValue();

            animateValues(weight_1, by_day);
            animateValues(weight_2, by_working_day);
            animateValues(weight_3, by_previous_working_day);
            refreshLayout.setRefreshing(false);
        }
    }

    private void animateValues(final float value, final TextView number_text) {
        ValueAnimator animator = ValueAnimator.ofFloat(
                Float.valueOf(number_text.getText().toString().replace("т", "").replace(" ", "").replace(",", ".")), value);
        animator.setDuration(1000);
        animator.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return 1-((1-input)*(1-input)*(1-input)*(1-input));
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if(getContext() != null)
                    number_text.setText(getContext().getResources().getString(R.string.tonne,
                            Float.parseFloat(animation.getAnimatedValue().toString())));
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                number_text.setText(new DecimalFormat("0.# т").format(Float.valueOf(number_text.
                        getText().toString().replace("т", "").replace(" ", "").replace(",", "."))));
            }
        });
        animator.start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PRESENTER_KEY, presenter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter.detachFragment();
    }
}
