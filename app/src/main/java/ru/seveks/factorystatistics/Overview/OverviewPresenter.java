package ru.seveks.factorystatistics.Overview;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

import ru.seveks.factorystatistics.OverviewFragment;

public class OverviewPresenter implements Parcelable {

    private OverviewFragment fragment;
    private OverviewModel model;
    private float weight_1 = 0, weight_2 = 0, weight_3 = 0;
    private ArrayList<Float> values;

    public OverviewPresenter(OverviewModel model) {
        this.model = model;
    }

    protected OverviewPresenter(Parcel in) {
        weight_1 = in.readFloat();
        weight_2 = in.readFloat();
        weight_3 = in.readFloat();
        values = (ArrayList<Float>) in.readSerializable();
        model = (OverviewModel) in.readSerializable();
    }

    public static final Creator<OverviewPresenter> CREATOR = new Creator<OverviewPresenter>() {
        @Override
        public OverviewPresenter createFromParcel(Parcel in) {
            return new OverviewPresenter(in);
        }

        @Override
        public OverviewPresenter[] newArray(int size) {
            return new OverviewPresenter[size];
        }
    };

    public void attachFragment(OverviewFragment fragment) {
        this.fragment = fragment;
    }

    public void detachFragment() {
        this.fragment = null;
    }

    public float getWeight_1() {
        return weight_1;
    }

    public float getWeight_2() {
        return weight_2;
    }

    public float getWeight_3() {
        return weight_3;
    }

    public ArrayList<Float> getValues() {
        return values;
    }

    public void getFilesInDirectory(){
        model.getFTPFiles(new OverviewModel.LoadFilesCallback() {
            @Override
            public void onLoad(ArrayList<Float> fields) {
                if (fragment != null) {
                    for (float i : fields) {
                        weight_1 += i;
                        if (i >= 7 && i < 19) weight_2 += i;
                        else weight_3 += i;
                    }
                    values = fields;
                    fragment.updateChart(fields, weight_1, weight_2, weight_3);
                }
            }
        });
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(weight_1);
        dest.writeFloat(weight_2);
        dest.writeFloat(weight_3);
        dest.writeSerializable(values);
        dest.writeSerializable(model);
    }
}
