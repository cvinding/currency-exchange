package com.example.testapp.model;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHandler {

    /**
     *
     * @param data
     * @param filename
     * @param context
     */
    public void writeToFile(String data, String filename, Context context) {
        try {
            File file = new File(context.getFilesDir(), filename);

            FileOutputStream stream = new FileOutputStream(file);

            stream.write(data.getBytes());

            stream.close();

        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /**
     *
     * @param filename
     * @param context
     * @return
     */
    public String readFromFile(String filename, Context context) {
        String content = "";

        try {
            File file = new File(context.getFilesDir(), filename);

            int length = (int) file.length();

            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);

            in.read(bytes);

            content = new String(bytes);

            in.close();
        } catch (FileNotFoundException e) {
            Log.e("Exception", "File does not exists " + e.toString());
        } catch (IOException e) {
            Log.e("Exception", "Unable to read file " + e.toString());
        }

        return content;
    }

    /**
     *
     * @param filename is the name of the given file
     * @return boolean
     */
    public boolean fileExists(String filename, Context context) {
        File path = context.getFilesDir();
        File file = new File(path, filename);
        return file.exists() && !file.isDirectory();
    }

}