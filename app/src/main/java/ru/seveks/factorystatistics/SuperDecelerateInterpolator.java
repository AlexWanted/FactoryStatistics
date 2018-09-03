package ru.seveks.factorystatistics;

import android.animation.TimeInterpolator;

public class SuperDecelerateInterpolator implements TimeInterpolator {

    @Override
    public float getInterpolation(float input) {
        return 1-((1-input)*(1-input)*(1-input)*(1-input));
    }
}
