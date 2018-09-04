package ru.seveks.factorystatistics;

import android.graphics.Color;

import java.util.ArrayList;

public class Globals {
    private static final Globals ourInstance = new Globals();

    public static Globals getInstance() {
        return ourInstance;
    }

    public int[] pieColors;

    private Globals() {
        pieColors = new int[] {
                Color.parseColor("#f44336"),
                Color.parseColor("#9c27b0"),
                Color.parseColor("#3f51b5"),
                Color.parseColor("#2196f3"),
                Color.parseColor("#009688"),
                Color.parseColor("#4caf50"),
                Color.parseColor("#ffeb3b"),
                Color.parseColor("#ffc107"),
                Color.parseColor("#ff9800"),
                Color.parseColor("#ff5722"),
                Color.parseColor("#795548"),
                Color.parseColor("#9e9e9e"),
                Color.parseColor("#607d8b")
        };
    }
}
