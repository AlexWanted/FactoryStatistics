package ru.seveks.factorystatistics.Overview;

import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;

import org.apache.commons.net.ftp.FTPFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

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
            public void onLoad(ArrayList<Float> fields) {
                fragment.updateChart(fields);
            }
        });
    }

}
