package ru.seveks.factorystatistics;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.seveks.factorystatistics.Views.PieChartView;

public class RecipesFragment extends Fragment {

    private static final String DATASET = "dataset";


    public RecipesFragment() {
        // Required empty public constructor
    }

    ArrayList<PieChartView.Recipe> pieValues;

    public static RecipesFragment newInstance(ArrayList<PieChartView.Recipe> pieValues) {
        RecipesFragment fragment = new RecipesFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DATASET, pieValues);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pieValues = (ArrayList<PieChartView.Recipe>) getArguments().getSerializable(DATASET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipes, container, false);
        PieChartView pieChartView = view.findViewById(R.id.pieChart);
        pieChartView.setValues(pieValues, false, false);
        return view;
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
