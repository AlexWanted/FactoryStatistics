package ru.seveks.factorystatistics.Overview;

import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPFile;

import ru.seveks.factorystatistics.OverviewFragment;

public class OverviewPresenter {

    private OverviewFragment fragment;
    private final OverviewModel model;

    public OverviewPresenter(OverviewModel model) {
        this.model = model;
    }

    public void attachFragment(OverviewFragment fragment) {
        this.fragment = fragment;
    }

    public void detachFragment() {
        this.fragment = null;
    }

    public void getFilesInDirectory(){
        model.getFTPFiles(new OverviewModel.LoadFilesCallback() {
            @Override
            public void onLoad(FTPFile[] files) {
                if(files != null)
                    for (FTPFile file : files) Log.d("FILES", String.valueOf(file.getName()));
            }
        });
    }

}
