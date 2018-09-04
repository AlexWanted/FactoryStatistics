package ru.seveks.factorystatistics;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.seveks.factorystatistics.Overview.OverviewModel;

public class SettingsFragment extends Fragment {

    ArrayList<String> dataset;
    AppCompatSpinner spinner;
    FoldersLoaderTask task;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        RelativeLayout settingsContainer = view.findViewById(R.id.close_settings);
        settingsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        spinner = view.findViewById(R.id.folder_spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        task = new FoldersLoaderTask(new FoldersLoaderTask.OnFolersLoadedListener() {
            @Override
            public void onFoldersLoaded(boolean isError, ArrayList<String> folders) {
                if (!isError) {
                    if (folders != null) {
                        dataset = folders;
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, dataset);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(spinnerArrayAdapter);

                    }
                }
            }
        });
        task.execute();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int statusBarHeight = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            ConstraintLayout.LayoutParams textParams = (ConstraintLayout.LayoutParams) settingsContainer.getLayoutParams();
            textParams.setMargins(0, statusBarHeight, 0, 0);
            settingsContainer.setLayoutParams(textParams);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null)
            ((MainActivity) getActivity()).setStatusBarTranslucent(true, Color.BLACK);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getActivity() != null)
            ((MainActivity)getActivity()).setStatusBarTranslucent(true, Color.WHITE);
        task.cancel(true);
        task = null;
    }

    private static class FoldersLoaderTask extends AsyncTask<Void, Void, ArrayList<String>> {

        interface OnFolersLoadedListener{
            void onFoldersLoaded(boolean isError, ArrayList<String> folders);
        }

        OnFolersLoadedListener listener;
        boolean isError;
        ArrayList<String> folders = new ArrayList<>();

        FoldersLoaderTask(OnFolersLoadedListener listener){
            this.listener = listener;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            isError = false;
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.setControlEncoding("Cp1251");
                ftpClient.connect("78.107.253.212", 21);

                if (ftpClient.login("korma", "3790")) {
                    FTPFile[] ftpFiles = ftpClient.listFiles("/USB_DISK/korma");
                    for (FTPFile file : ftpFiles) {
                        if (file.isDirectory()) {
                            folders.add(file.getName());
                        }
                    }
                    return folders;
                } else {
                    isError = true;
                    Log.e("DB", "Failed to authorize");

                }
            } catch (Exception e) {
                e.printStackTrace();
                isError = true;
                Log.e("DB", "Failed to connect");
            } finally {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> folders) {
            if (listener != null) listener.onFoldersLoaded(isError, folders);
            super.onPostExecute(folders);
        }
    }
}
