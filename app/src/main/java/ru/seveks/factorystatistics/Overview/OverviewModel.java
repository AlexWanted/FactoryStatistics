package ru.seveks.factorystatistics.Overview;

import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class OverviewModel {

    public OverviewModel() {
    }

    public void getFTPFiles(LoadFilesCallback callback){
        new FTPTask(callback).execute();
    }

    interface LoadFilesCallback{
        void onLoad(FTPFile[] files);
    }

    private class FTPTask extends AsyncTask<Void, Void, Void> {

        LoadFilesCallback callback;
        FTPFile[] files;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        FTPTask(LoadFilesCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            FTPClient ftpClient;
            try {
                ftpClient = new FTPClient();
                ftpClient.connect("192.168.0.1");

                if(ftpClient.login("korma", "3790"))
                    files = ftpClient.listFiles("/USB_DISK/korma");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            callback.onLoad(files);
        }
    }
}
