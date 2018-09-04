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
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.seveks.factorystatistics.Views.PieChartView;

public class OverviewModel implements Serializable {

    private static final String TAG = OverviewModel.class.getSimpleName();

    String folderName = "Test Factory";

    public OverviewModel() {
    }

    public void getAvailableDates(LoadedDatesCallback callback){
        new DatesLoaderTask(folderName, callback).execute(folderName);
    }

    public void getFiles(String date, LoadFilesCallback callback){
        new FilesObtainerTask(folderName, date, callback).execute();
    }

    interface LoadedDatesCallback{
        void onLoaded(boolean isError, ArrayList<Date> dates);
    }

    interface LoadFilesCallback{
        void onLoad(boolean isError, ArrayList<Float> barFields, ArrayList<PieChartView.Recipe> pieFields);
    }

    private static class DatesLoaderTask extends AsyncTask<String, Void, ArrayList<Date>> {

        boolean isError;
        String folderName;
        LoadedDatesCallback callback;

        DatesLoaderTask(String folderName, LoadedDatesCallback callback){
            this.folderName = folderName;
            this.callback = callback;
        }

        @Override
        protected ArrayList<Date> doInBackground(String... strings) {
            isError = false;
            String folderName = strings[0];
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.setControlEncoding("Cp1251");
                ftpClient.connect("78.107.253.212", 21);

                if (ftpClient.login("korma", "3790")) {
                    FTPFile[] ftpFiles = ftpClient.listFiles("/USB_DISK/korma/"+folderName);
                    ArrayList<Date> dates = new ArrayList<>();
                    for (FTPFile file : ftpFiles) {
                        Matcher matcher = Pattern.compile("_(\\d\\d?)_(\\d\\d?)_(\\d\\d?)\\.").matcher(file.getName());
                        while (matcher.find()) {
                            Calendar calendar = Calendar.getInstance();
                            int year = Integer.valueOf(matcher.group(3));
                            int month = Integer.valueOf(matcher.group(2));
                            int date = Integer.valueOf(matcher.group(1));
                            calendar.set(year + 2000, month - 1, date);
                            dates.add(new Date(calendar.getTimeInMillis()));
                        }
                    }
                    return dates;
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
        protected void onPostExecute(ArrayList<Date> dates) {
            super.onPostExecute(dates);
            callback.onLoaded(isError, dates);
        }
    }

    private static class FilesObtainerTask extends AsyncTask<Void, Void, Void> {

        LoadFilesCallback callback;
        ArrayList<Float> barGraphValues;
        ArrayList<PieChartView.Recipe> pieGraphValues;
        boolean isError = false;
        FTPClient ftpClient;
        String folderName, date;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        FilesObtainerTask(String folderName, String date, LoadFilesCallback callback) {
            this.folderName = folderName;
            this.date = date;
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
                    FTPFile[] ftpFiles = ftpClient.listFiles("/USB_DISK/korma/"+folderName);
                    if (date == null) {
                        ArrayList<Date> dates = new ArrayList<>();
                        for (FTPFile file : ftpFiles) {
                            Matcher matcher = Pattern.compile("_(\\d\\d?)_(\\d\\d?)_(\\d\\d?)\\.").matcher(file.getName());
                            while (matcher.find()) {
                                Calendar calendar = Calendar.getInstance();
                                int year = Integer.valueOf(matcher.group(3));
                                int month = Integer.valueOf(matcher.group(2));
                                int date = Integer.valueOf(matcher.group(1));
                                calendar.set(year + 2000, month - 1, date);
                                dates.add(new Date(calendar.getTimeInMillis()));
                            }
                        }
                        date = getMostRecentDate(dates);
                    }
                    for (FTPFile file : ftpFiles){
                        if (file.getName().contains(date)) {
                            InputStream inputStream = ftpClient.retrieveFileStream("/USB_DISK/korma/"+folderName+"/" + file.getName());
                            InputStreamReader reader = new InputStreamReader(inputStream, "Cp1251");
                            int data;
                            String result = "";
                            while ((data = reader.read()) != -1) {
                                result = result + (char) data;
                            }
                            if (file.getName().contains("volume_rec")) {
                                Pattern linePattern = Pattern.compile("\\d+\\s{4}(.*)\\s{5,}([\\d,]+)");
                                Matcher matcher = linePattern.matcher(result);
                                while (matcher.find()) {
                                    String name = matcher.group(1).replaceAll(",","");
                                    name = name.substring(0,1).toUpperCase()+name.substring(1,name.length());
                                    name = name.trim();
                                    String valueStr = matcher.group(2).replaceAll("\\s", "");
                                    float value = Float.valueOf(valueStr.replace(",","."));
                                    PieChartView.Recipe recipe = new PieChartView.Recipe(name, value);
                                    pieGraphValues.add(recipe);
                                }
                            }
                            if (file.getName().contains("volume_time")) {
                                Pattern linePattern = Pattern.compile("\\d+\\s+(\\d+,\\d)");
                                Matcher matcher = linePattern.matcher(result);
                                while (matcher.find()) {
                                    float value = Float.parseFloat(matcher.group(1).replace(",", "."));
                                    barGraphValues.add(value);
                                }
                            }
                            while (!ftpClient.completePendingCommand());
                            inputStream.close();
                            reader.close();
                        }
                    }
                } else {
                    isError = true;
                    barGraphValues.clear();
                    for (int i = 0; i < 24; i++) {
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
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            callback.onLoad(isError, barGraphValues, pieGraphValues);

        }
    }

    private static String getMostRecentDate(ArrayList<Date> dates){
        long maxValue = 0;
        for (Date date: dates){
            if (maxValue < date.getTime()) maxValue = date.getTime();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(maxValue);
        calendar.get(Calendar.DAY_OF_MONTH);
        calendar.get(Calendar.MONTH);
        calendar.get(Calendar.YEAR);
        return String.format("%1$td_%1$tm_%1$ty", calendar);
    }
}
