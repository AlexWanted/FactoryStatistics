package ru.seveks.factorystatistics.Overview;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.seveks.factorystatistics.Views.PieChartView;

public class OverviewModel implements Serializable {

    private static final String TAG = OverviewModel.class.getSimpleName();

    public OverviewModel() {
    }

    public void getFTPFiles(LoadFilesCallback callback){
        new FTPTask(callback).execute();
    }

    interface LoadFilesCallback{
        void onLoad(boolean isError, ArrayList<Float> barFields, ArrayList<PieChartView.Recipe> pieFields);
    }

    private class FTPTask extends AsyncTask<Void, Void, Void> {

        LoadFilesCallback callback;
        ArrayList<Float> barGraphValues;
        ArrayList<PieChartView.Recipe> pieGraphValues;
        boolean isError = false;
        FTPClient ftpClient;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        FTPTask(LoadFilesCallback callback) {
            this.callback = callback;
            this.barGraphValues = new ArrayList<>();
            this.pieGraphValues = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ftpClient = new FTPClient();
                ftpClient.setControlEncoding("Cp1251");
                ftpClient.connect("78.107.253.212", 21);

                if(ftpClient.login("korma", "3790")) {
                    FTPFile[] ftpFiles = ftpClient.listFiles("/USB_DISK/korma/");
                    for (FTPFile file : ftpFiles) {
                        if (file.getName().endsWith("18.txt")) {
                            InputStream inputStream = ftpClient.retrieveFileStream("/USB_DISK/korma/" + file.getName());
                            InputStreamReader reader = new InputStreamReader(inputStream, "Cp1251");
                            int data;
                            String result = "";
                            int index = 0;
                            while ((data = reader.read()) != -1) {
                                index++;
                                result = result + (char) data;
                            }
                            if (file.getName().equals("volume_rec_31_08_18.txt")) {
                                Pattern linePattern = Pattern.compile("\\d+\\s{4}(.*)\\s{5,}([\\d,]+)");
                                Matcher matcher = linePattern.matcher(result);
                                while (matcher.find()) {
                                    String name = matcher.group(1).replaceAll(",","");
                                    name = name.substring(0,1).toUpperCase()+name.substring(1,name.length());
                                    String valueStr = matcher.group(2).replaceAll("\\s", "");
                                    float value = Float.valueOf(valueStr.replace(",","."));
                                    PieChartView.Recipe recipe = new PieChartView.Recipe(name, value);
                                    pieGraphValues.add(recipe);
                                }
                            } else if (file.getName().equals("volume_time_31_08_18.txt")) {
                                Pattern linePattern = Pattern.compile("\\d+\\s+(\\d+,\\d)");
                                Matcher matcher = linePattern.matcher(result);
                                while (matcher.find()) {
                                    float value = Float.parseFloat(matcher.group(1).replace(",", "."));
                                    barGraphValues.add(value);
                                }
                            }

                            inputStream.close();
                            reader.close();
                            while (!ftpClient.completePendingCommand());
                        }
                    }
                } else {
                    isError = true;
                    barGraphValues.clear();
                    for (int i = 0; i < 24; i++){
                        barGraphValues.add(0f);
                        PieChartView.Recipe recipe = new PieChartView.Recipe(String.valueOf(i), 0f);
                        pieGraphValues.add(recipe);
                    }
                    Log.e("DB", "Failed to authorize");

                }
            } catch (Exception e) {
                e.printStackTrace();
                isError = true;
                barGraphValues.clear();
                for (int i = 0; i < 24; i++) {
                    barGraphValues.add(0f);
                    PieChartView.Recipe recipe = new PieChartView.Recipe(String.valueOf(i), 0f);
                    pieGraphValues.add(recipe);
                }
                Log.e("DB", "Failed to connect");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            callback.onLoad(isError, barGraphValues, pieGraphValues);
        }
    }
}
