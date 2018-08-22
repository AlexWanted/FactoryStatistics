package ru.seveks.factorystatistics;

import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    public static final String  OVERVIEW_FRAGMENT = "overview";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment mainSettingsFragment = new OverviewFragment();
        ft.replace(R.id.fragments_container, mainSettingsFragment, OVERVIEW_FRAGMENT);
        ft.commit();
    }

    /**
     * Метод для изменения прозрачности и цвета статус бара.
     * Не работает на версии SDK меньше 23.
     *
     * @param enable - включена ли прозрачность. [true, false]
     * @param color - цвет статус бара. [Color.WHITE, Color.BLACK]
     */
    protected void setStatusBarTranslucent(boolean enable, int color) {
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (enable) {
                getWindow().getDecorView().setSystemUiVisibility(
                        color == Color.BLACK ? (flags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                                             : (flags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(flags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
        }
    }
}
