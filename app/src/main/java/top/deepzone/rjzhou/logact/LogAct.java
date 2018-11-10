package top.deepzone.rjzhou.logact;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import top.deepzone.rjzhou.logact.model.ConnectionModel;

public class LogAct {
    // 提交问题示例
    // imei 是必须的，其他可以添加如 gps位置， 问题描述等
    static public void examplerReport() throws Exception {
        Map<String, String> fields = new HashMap<String, String>();
        fields.put("imei", "11223344");
        fields.put("log", "an error occurs");
        reportException(fields);
    }

    // 报告问题
    // imei 项必须存在
    static public void reportException(Map<String, String> fields) throws Exception {
        post(ConnectionModel.getHttpUri() + "/log/report", fields);
    }

    static public UploadTask[] uploadLogTasks(
            final String filepath,
            final Map<String, String> fieldsMap) {
        return uploadLogTasks(filepath, fieldsMap, 1024 * 1024);
    }

    // 将大文件上传切分为多个小上传任务
    // 切分后的任务应按顺序执行
    static public UploadTask[] uploadLogTasks(
            final String filepath,
            final Map<String, String> fieldsMap,
            final long trunkSize) {
        String url = ConnectionModel.getHttpUri() + "/log/upload";
        return uploadTasks(url, filepath, fieldsMap, trunkSize);
    }

    // 下载进度指示
    static public interface DownloadProgress {
        public void onDownloading(long lastPost);
    }

    // 下载文件，如果中间出错，那么记录下最后的位置 (通过DownloadProgress)
    // 下次只需要再从上次的位置开始， 设置 startOffset
    static public void download(String url, String target, long startOffset, DownloadProgress prog) throws Exception {

        RandomAccessFile randomFile = new RandomAccessFile(target, "rw");
        randomFile.seek(startOffset);

        long lastDownPos = startOffset;

        InputStream ins = get(url, startOffset);

        byte[] buffer = new byte[4096];
        int len = -1;
        while((len = ins.read(buffer)) != -1){
            lastDownPos  += len;
            randomFile.write(buffer, 0, len);
            if (prog != null) {
                prog.onDownloading(lastDownPos);
            }
        }
        randomFile.close();
    }

    static public String getContent(String url) throws Exception {
        InputStream ins = get(url, 0);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(ins));
        String lines;
        String result = "";
        while ((lines = reader.readLine()) != null) {
            result += lines;
        }
        reader.close();

        return result;
    }

    static public InputStream get(String url, long startOffset) throws Exception {
        URL actinoUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) actinoUrl.openConnection();
        connection.setDoOutput(false);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Connection", "Keep-Alive");

        if (startOffset > 0) {
            connection.setRequestProperty("Range", "bytes=" + String.valueOf(startOffset) + "-");
        }

        connection.connect();

        int code = connection.getResponseCode();
        if (code/100 != 2) {
            throw new Exception("Bad response code " + String.valueOf(code));
        }

        return connection.getInputStream();
    }

    static public void post(String url, Map<String, String> fieldsMap) throws Exception {
        URL actinoUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) actinoUrl.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        connection.connect();

        DataOutputStream out = new DataOutputStream(connection
                .getOutputStream());

        if (fieldsMap != null) {
            StringBuffer strBuf = new StringBuffer();
            Iterator<Map.Entry<String, String>> iter = fieldsMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                String field = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (strBuf.length() > 0) {
                    strBuf.append("&");
                }
                strBuf.append(URLEncoder.encode(field, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
            }
            out.write(strBuf.toString().getBytes());
        }

        out.flush();
        out.close();

        checkHttpStatus(connection);
    }

    public interface UploadTask {
        void upload() throws Exception;
    }
    static UploadTask[] uploadTasks(final String url,
                                    final String filepath,
                                    final Map<String, String> fieldsMap,
                                    final long trunkSize) {

        if (trunkSize <= 0) {
            return new UploadTask[]{
                    new UploadTask() {
                        public void upload() throws Exception {
                            LogAct.upload(url, filepath, fieldsMap);
                        }
                    }
            };
        }

        File f = new File(filepath);
        long length = f.length();
        int count = (int)((length + trunkSize - 1) / trunkSize);

        UploadTask[] tasks = new UploadTask[count];

        for (int i = 0; i < count; i++) {
            final long offset = i * trunkSize;
                UploadTask task = new UploadTask() {
                    public void upload() throws Exception {
                        LogAct.upload(url, filepath, offset, fieldsMap, trunkSize);
                    }
                };

            tasks[i] = task;
        }
        return tasks;
    }

    static void upload(String url,
                       String filepath,
                       Map<String, String> fieldsMap) throws Exception {
        upload(url, filepath, 0, fieldsMap, -1);
    }

    static void upload(String url,
                       String filepath,
                       final long offset,
                       Map<String, String> fieldsMap,
                       final long trunkSize) throws Exception {
        final String BOUNDARY = "---------------------------123821742118716";

        boolean useTrunks = trunkSize > 0;

        File f = new File(filepath);
        long length = f.length();
        final long leftSize = length - offset;
        boolean eot = false;
        int trunkIndex = 1;

        if (trunkSize > 0) {
            if (offset % trunkSize != 0) {
                throw new Exception("offset: " + String.valueOf(offset) + " not aligned with trunkSize: " + String.valueOf(trunkSize));
            }
            trunkIndex = (int)(offset / trunkSize) + 1;
        }

        long totalReadSize = trunkSize;
        if (totalReadSize <= 0) {
            totalReadSize = leftSize;
        }

        if (totalReadSize >= leftSize) {
            totalReadSize = leftSize;
            eot = true;
            if (offset == 0) {
                useTrunks = false;
            }
        }

        URL actionUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) actionUrl.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");

        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        OutputStream out = new DataOutputStream(conn.getOutputStream());
        StringBuffer strBuf = new StringBuffer();
        if (fieldsMap != null) {
            for (Map.Entry<String, String> entry : fieldsMap.entrySet()) {
                String field = entry.getKey();
                String value = entry.getValue();
                strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                strBuf.append("Content-Disposition: form-data; name=\"" + field + "\"\r\n\r\n");
                strBuf.append(value);
            }
        }

        if (useTrunks) {
            strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
            strBuf.append("Content-Disposition: form-data; name=\"trunks\"\r\n\r\n");
            strBuf.append(String.valueOf(trunkIndex));

            if (eot) {
                strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                strBuf.append("Content-Disposition: form-data; name=\"eot\"\r\n\r\n1");
            }
        }

        String filename = f.getName();
        strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
        strBuf.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n");
        strBuf.append("Content-Type:application/octet-stream\r\n\r\n");

        out.write(strBuf.toString().getBytes());

        DataInputStream in = new DataInputStream(new FileInputStream(f));
        in.skip(offset);
        int bytes = 0;
        long left = totalReadSize;
        byte[] bufferOut = new byte[4096];
        int towrite = left < 4096 ? (int)left : 4096;
        while (towrite > 0 && (bytes = in.read(bufferOut, 0, towrite)) != -1) {
            out.write(bufferOut, 0, bytes);
            left -= bytes;
            towrite = left < 4096 ? (int)left : 4096;
        }
        in.close();

        byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();

        out.write(endData);
        out.flush();
        out.close();

        checkHttpStatus(conn);
    }

    private static void checkHttpStatus(HttpURLConnection connection) throws Exception {
        boolean ret = connection.getResponseCode() == 200;
        if (!ret) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String result = reader.readLine();
            throw new Exception(result);
        }
    }
}
