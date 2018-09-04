package ru.seveks.factorystatistics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ru.seveks.factorystatistics.Database.DBHelper;
import ru.seveks.factorystatistics.Overview.OverviewPresenter;
import ru.seveks.factorystatistics.Views.BarChartView;
import ru.seveks.factorystatistics.Views.PieChartView;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class OverviewFragment extends Fragment {

    private final String PRESENTER_KEY = "presenter";
    private OverviewPresenter presenter;
    BarChartView barChartView;
    PieChartView pieChartView;
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

        refreshLayout = view.findViewById(R.id.refresh);
        refreshLayout.setEnabled(false);
        final ImageView refreshButton = view.findViewById(R.id.refreshButton);


        ImageView settings = view.findViewById(R.id.settings);
        by_day = view.findViewById(R.id.number_by_day);
        by_working_day = view.findViewById(R.id.number_by_working_day);
        by_previous_working_day = view.findViewById(R.id.number_by_previous_working_day);
        barChartView = view.findViewById(R.id.barChart);
        pieChartView = view.findViewById(R.id.pieChart);


        if (savedInstanceState != null) {
            presenter = savedInstanceState.getParcelable(PRESENTER_KEY);
            if (presenter != null && getContext() != null) {
                presenter.attachFragment(this);
                by_day.setText(getContext().getResources().getString(R.string.tonne, presenter.getWeight_1()));
                by_working_day.setText(getContext().getResources().getString(R.string.tonne, presenter.getWeight_2()));
                by_previous_working_day.setText(getContext().getResources().getString(R.string.tonne, presenter.getWeight_3()));
                if (barChartView != null)
                    barChartView.setValues(presenter.getBarValues(), false, false);
            }
        } else {
            presenter = new OverviewPresenter();
            presenter.attachFragment(this);
            presenter.getFilesInDirectory();
            refreshLayout.setRefreshing(true);
        }

        barChartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity().getSupportFragmentManager().findFragmentByTag("charts") == null) {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment hoursFragment = HoursFragment.newInstance(presenter.getBarValues());
                    ft.addToBackStack("charts");
                    ft.setCustomAnimations(R.anim.slide_in_to_top, R.anim.slide_out_to_bottom, R.anim.slide_in_to_top, R.anim.slide_out_to_bottom);
                    //ft.hide(getActivity().getSupportFragmentManager().findFragmentByTag("overview"));
                    ft.add(R.id.fragments_container, hoursFragment, "charts");
                    ft.commit();
                }
            }
        });

        pieChartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity().getSupportFragmentManager().findFragmentByTag("recipes") == null) {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment hoursFragment = RecipesFragment.newInstance(pieChartView.getValues());
                    ft.addToBackStack("recipes");
                    ft.setCustomAnimations(R.anim.slide_in_to_top, R.anim.slide_out_to_bottom, R.anim.slide_in_to_top, R.anim.slide_out_to_bottom);
                    ft.add(R.id.fragments_container, hoursFragment, "recipes");
                    ft.commit();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LinearLayout textContainer = view.findViewById(R.id.header_text_container);
            int statusBarHeight = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            ConstraintLayout.LayoutParams settingsParams = (ConstraintLayout.LayoutParams) settings.getLayoutParams();
            ConstraintLayout.LayoutParams refreshParams = (ConstraintLayout.LayoutParams) refreshButton.getLayoutParams();
            ConstraintLayout.LayoutParams textParams = (ConstraintLayout.LayoutParams) textContainer.getLayoutParams();
            settingsParams.setMargins(0, statusBarHeight, 0, 0);
            refreshParams.setMargins(0, statusBarHeight, 0, 0);
            textParams.setMargins(0, statusBarHeight, 0, 0);
            settings.setLayoutParams(settingsParams);
            textContainer.setLayoutParams(textParams);
            refreshButton.setLayoutParams(refreshParams);
        }

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(true);
                    presenter.getFilesInDirectory();
                }
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
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

        /*refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.getFilesInDirectory();
            }
        });*/
        return view;
    }

    public void onConnectionError() {
        Toast.makeText(getContext(), "Ошибка подключения", Toast.LENGTH_SHORT).show();
        refreshLayout.setRefreshing(false);

    }
    public void updateChart(ArrayList<Float> barValues, ArrayList<PieChartView.Recipe> pieValues, float weight_1, float weight_2, float weight_3) {
        if (barChartView != null) {
            presenter.setRecipesValues(pieValues);
            pieChartView.setValues(pieValues, true, true);
            if (prevMaxBarValue <= 1) barChartView.setValues(barValues, true, false);
            else barChartView.setValues(barValues, true, true);
            prevMaxBarValue = barChartView.getMaxBarValue();

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
