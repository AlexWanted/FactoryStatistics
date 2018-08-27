package ru.seveks.factorystatistics.Overview;

import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;
import com.linuxense.javadbf.DBFUtils;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class OverviewModel implements Serializable {

    public OverviewModel() {
    }

    public void getFTPFiles(LoadFilesCallback callback){
        new FTPTask(callback).execute();
    }

    interface LoadFilesCallback{
        void onLoad(boolean isError, ArrayList<Float> fields);
    }

    private class FTPTask extends AsyncTask<Void, Void, Void> {

        LoadFilesCallback callback;
        InputStream inputStream;
        ArrayList<Float> map;
        boolean isError = false;
        FTPClient ftpClient;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        FTPTask(LoadFilesCallback callback) {
            this.callback = callback;
            this.map = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ftpClient = new FTPClient();
                ftpClient.connect("78.107.253.212", 21);

                if(ftpClient.login("korma", "3790")) {
                    FTPFile[] ftpFiles = ftpClient.listFiles("/USB_DISK/korma/");
                    for (FTPFile file : ftpFiles) {
                        if (file.getName().equals("month03-15.dbf")) {
                            inputStream = ftpClient.retrieveFileStream("/USB_DISK/korma/"+file.getName());
                            Log.d("DB", "Found file");
                            DBFReader reader = null;
                            try {
                                reader = new DBFReader(inputStream);
                                DBFRow row;
                                int day = new Random(System.currentTimeMillis()).nextInt(13);
                                Log.d("DB", "Day "+(day+1));
                                while ((row = reader.nextRow()) != null) {
                                    if (row.getInt("DAY") == day+1) {
                                        for (int i = 1; i <= 24; i++) {
                                            if (row.getString("H" + i) != null)
                                                map.add(row.getFloat("H" + i)/1000);
                                            else
                                                map.add(0f);
                                        }
                                    }
                                }
                                Log.d("DB", "Successfully parsed file");
                            } catch (Exception e) {
                                isError = true;
                                map.clear();
                                for (int i = 0; i < 24; i++)
                                    map.add(0f);
                                Log.e("DB", "Failed to parse file");
                                e.printStackTrace();
                            } finally {
                                DBFUtils.close(reader);
                            }
                        }
                    }
                } else {
                    isError = true;
                    map.clear();
                    for (int i = 0; i < 24; i++)
                        map.add(0f);
                    Log.e("DB", "Failed to authorize");
                }
            } catch (Exception e) {
                e.printStackTrace();
                isError = true;
                map.clear();
                for (int i = 0; i < 24; i++)
                    map.add(0f);
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
            callback.onLoad(isError, map);
        }
    }
}
