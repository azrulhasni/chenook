/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.utils;

import com.azrul.chenook.config.SearchConfig;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azrul
 */
public class PropertyUtils {

    private static Properties properties;

    static {
        properties = new Properties();
        URL url = new PropertyUtils().getClass().getClassLoader().getResource("application.properties");
        try {
            properties.load(new FileInputStream(url.getPath()));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PropertyUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PropertyUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

}
