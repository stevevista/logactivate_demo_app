package top.deepzone.rjzhou.logact.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import top.deepzone.rjzhou.logact.LogAct;
import top.deepzone.rjzhou.logact.Notify;
import top.deepzone.rjzhou.logact.R;
import top.deepzone.rjzhou.logact.model.ConnectionModel;

public class LogFragment extends Fragment {

    private static final String TAG = "LogFragment";

    public LogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        final View rootView = inflater.inflate(R.layout.fragment_log, container, false);

        rootView.findViewById(R.id.normal_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        upload(0);
                    }
                }).start();
            }
        });

        rootView.findViewById(R.id.trunk_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        upload(100);
                    }
                }).start();
            }
        });

        rootView.findViewById(R.id.ota_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String result = "";
                        try {
                            result = LogAct.getContent(ConnectionModel.getHttpUri() + "/ota/version");
                        } catch (Exception e) {
                            result = e.getMessage();
                        }

                        Message message = new Message();
                        message.what = 1;
                        message.obj = result;
                        mHandler.sendMessage(message);
                    }
                }).start();
            }
        });

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                .checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission
                            .WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    private void upload(final long trunkSize) {
        String result = "OK";
        try {
            createDummpFile();

            Map<String, String> fields = new HashMap<>();
            fields.put("imei", "11223344");

            LogAct.UploadTask[] tasks = LogAct.uploadLogTasks(dummpyFilePath, fields, trunkSize);
            for (LogAct.UploadTask t : tasks) {
                t.upload();
            }

        } catch (Exception e) {
            result = e.getMessage();
        }

        Message message = new Message();
        message.what = 1;
        message.obj = result;
        mHandler.sendMessage(message);
    }

    private Handler mHandler = new Handler(){
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                Notify.toast(getContext(), (String)msg.obj, Toast.LENGTH_SHORT);
            }
        }
    };

    private final String dummpyFilePath = Environment.getExternalStorageDirectory().getPath()+ "/dummy.bin";
    private final int REQUEST_PERMISSIONS = 1;

    private void createDummpFile() throws IOException {
        File f = new File(dummpyFilePath);
        if (f.exists() && f.length() >= 1000) {
            return;
        }
        FileOutputStream fos = new FileOutputStream(f);
        for (int i=0; i<100; i++)
            fos.write("abcdefghij".getBytes());
        fos.close();
    }
}
