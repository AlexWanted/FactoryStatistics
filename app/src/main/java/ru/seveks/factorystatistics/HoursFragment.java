package ru.seveks.factorystatistics;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.seveks.factorystatistics.Views.BarChartView;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HoursFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HoursFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HoursFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ArrayList<Float> dataset;

    public HoursFragment() {
        // Required empty public constructor
    }

    public static HoursFragment newInstance(ArrayList<Float> data) {
        Bundle args = new Bundle();
        args.putSerializable("data", data);
        HoursFragment fragment = new HoursFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataset = (ArrayList<Float>) getArguments().getSerializable("data");
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hours, container, false);
        BarChartView barChartView = view.findViewById(R.id.barChart);
        if (dataset != null)
            barChartView.setValues(dataset, false, false);


        /*ShapeOfView shapeOfView = view.findViewById(R.id.myShape);
        shapeOfView.setClipPathCreator(new ClipPathManager.ClipPathCreator() {
            @Override
            public Path createClipPath(int width, int height) {
                final Path path = new Path();

                //eg: triangle
                path.moveTo(0, 0);
                path.lineTo(0.5 * width, height);
                path.lineTo(width, 0);
                path.close();

                return path;
            }
        });*/
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null)
            ((MainActivity)getActivity()).setStatusBarTranslucent(true, Color.BLACK);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        /*if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }*/
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getActivity() != null) {
            if(getActivity().getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT)
                ((MainActivity) getActivity()).setStatusBarTranslucent(true, Color.WHITE);
            else
                ((MainActivity) getActivity()).setStatusBarTranslucent(false, Color.WHITE);
        }
        //mListener = null;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
