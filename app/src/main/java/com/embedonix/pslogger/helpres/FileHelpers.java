package com.embedonix.pslogger.helpres;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Saeid on 12-5-2014.
 */
public class FileHelpers {

    public static final String TAG = "FileHelpers";


    /**
     * Reads a file and returns its content as byte array
     *
     * @param file file that should be returned as byte array
     * @return byte[] array of bytes of the file
     */
    public static byte[] convertTextFileToByteArray(File file) throws FileNotFoundException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException("File " + file.getAbsolutePath() + " does not exist");
        }
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bFile;
    }

    /**
     * @param filename
     * @param input
     * @return
     * @throws java.io.IOException
     */
    public static byte[] zipBytes(String filename, byte[] input) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry(filename);
        entry.setSize(input.length);
        zos.putNextEntry(entry);
        zos.write(input);
        zos.closeEntry();
        zos.close();
        return baos.toByteArray();
    }

    public static String compressAndBase64(byte[] byteArray)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(baos);
        zos.write(byteArray);
        zos.close();
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * This method will return an array of unprocessed accelerometer logs from
     * <code>AppConstants.LOG_FOLDER_NAME</code>
     *
     * @return
     */
    public static List<File> getListOfNotUploadedFiles(Context context)
            throws NullPointerException {

        List<File> files = new ArrayList<File>();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader bufferedReader = null;

        try {
            fis = context.openFileInput(AppConstants.LOG_WORK_LIST_FILENAME);
            isr = new InputStreamReader(fis);
            bufferedReader = new BufferedReader(isr);
            String line;
            while((line = bufferedReader.readLine()) != null) {
                File file = new File(line);
                if(file.exists()) {
                    files.add(file);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(files.size() > 0) {
            return files;
        } else {
            boolean deleted = context.deleteFile(AppConstants.LOG_WORK_LIST_FILENAME);
            throw new NullPointerException("none of the files are available");
        }
    }

    public static void addFileToNotUploadedFiles(Context context, File file){

        FileOutputStream fos = null;
        FileWriter writer = null;

        try {
            fos = context.openFileOutput(AppConstants.LOG_WORK_LIST_FILENAME
                    , Context.MODE_APPEND);
            String filePath = file.getPath();
            byte[] buffer = (filePath + "\n").getBytes();
            Log.i(TAG, "file is" + filePath);
            fos.write(buffer);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static String getNewFileName(long timestamp, String appName) {
        String fileName = "";
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        fileName += c.get(Calendar.YEAR) + "_" + (c.get(Calendar.MONTH) + 1)//Java month is 0 based
                + "_" + c.get(Calendar.DAY_OF_MONTH) + "_" + c.get(Calendar.HOUR_OF_DAY)
                + "_" + c.get(Calendar.MINUTE) + "_" + c.get(Calendar.SECOND);
        fileName += "_" + appName.replace(" ", "_") + ".csv";
        return fileName;
    }

    public static String removeExtensionFromFileName(String fileNameWithExtension) {
        String fileName = fileNameWithExtension;
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }

        return fileName;
    }

    /**
     * <p>Logs a critical error in the app designated folder and log file
     * <p/>
     * by default it goes to ExternalStorage -> AppConstants.LOG_FOLDER_NAME
     * -> AppConstants.APP_LOG_FILENAME
     * </p>
     *  @param millis  local timestamp of error
     * @param logType {@link AppLogType} type of message
     * @param callerTag
     * @param message message about error
     */
    public static void appendToAppLog(final long millis, final AppLogType logType
            , final String callerTag, final String message) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                FileWriter writer = null;
                try {

                    File dir = new File(Environment.getExternalStorageDirectory() + File.separator
                            + AppConstants.LOG_FOLDER_NAME);
                    dir.mkdir();

                    File file = new File(dir, AppConstants.APP_LOG_FILENAME);

                    if (!file.exists()) {
                        file.createNewFile();
                    }


                    writer = new FileWriter(file, true);
                    writer.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(millis)
                           + "\t" + logType + "\t" + callerTag + "\t" + message + "\r\n");
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();

    }

    public static void compressAndStoreFile(final Context context, final File file
            , final boolean addToNotUploadedFilesList) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {

                Log.i(TAG, "going to compress and store " + file.getName());

                File toCompress = new File(file.getParent(), file.getName() + ".gz");
                FileOutputStream fos = null;
                GZIPOutputStream zos = null;

                try {
                    byte[] content = FileHelpers.convertTextFileToByteArray(file);
                    fos = new FileOutputStream(toCompress);
                    zos = new GZIPOutputStream(new BufferedOutputStream(fos));
                    zos.write(content);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (zos != null) {
                            zos.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(addToNotUploadedFilesList) {
                    FileHelpers.addFileToNotUploadedFiles(context, toCompress);
                }

                return file.delete();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    Log.i(TAG, "compressed and deleted old file");
                }
            }
        }.execute();
    }

    /**
     * Generates a String like 2012_July
     *
     * @param millis timestamp to create String from
     * @return
     */
    public static String getFolderForMonth(long millis) {
        return new SimpleDateFormat("yyyy_MMMM").format(millis);
    }

    /**
     * Generates a String like 01_Tuesday
     * @param millis
     * @return
     */
    public static String getFolderForDay(long millis){
        return new SimpleDateFormat("dd_EEEE").format(millis);
    }

    public static File getAppLog() {

        try {
            File dir = new File(Environment.getExternalStorageDirectory() + File.separator
                    + AppConstants.LOG_FOLDER_NAME);
            File file = new File(dir, AppConstants.APP_LOG_FILENAME);

            if(file.exists() && file.length() > 0) {
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
