package com.jty.backtrack.data_parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Config {
    private static final String FILE_NAME = "BacktrackDataParser.properties";

    private final Properties mProperties = new Properties();

    public Config() {
        checkFile();
        load();
    }

    public String get(String key,String defaultValue) {
        return mProperties.getProperty(key,defaultValue);
    }

    public void set(String key, String value) {
        mProperties.put(key, value);
    }

    private void checkFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void load() {
        FileInputStream inFile = null;
        InputStreamReader in = null;
        try {
            inFile = new FileInputStream(FILE_NAME);
            in = new InputStreamReader(inFile, StandardCharsets.UTF_8);
            mProperties.load(in);
            //log
            for (String key : mProperties.stringPropertyNames()) {
                String info = mProperties.getProperty(key);
                System.out.println(key + ": " + info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (inFile != null) {
                    inFile.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void save() {
        FileOutputStream oFile = null;
        OutputStreamWriter out = null;
        try {
            oFile = new FileOutputStream(FILE_NAME, false);
            out = new OutputStreamWriter(oFile, StandardCharsets.UTF_8);
            mProperties.store(out, "配置保存完成");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (oFile != null) {
                    oFile.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }
}
