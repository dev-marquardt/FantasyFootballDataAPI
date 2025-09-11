package com.dev.marquardt;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class Settings {


    private static final Properties properties = new  Properties();

    protected static final String dbURL;

    protected static final String dbUser;

    protected static final String dbPass;

    protected static final String sleeperURL;

    static {
            try{
                var input = Data.class.getClassLoader().getResourceAsStream("application.properties");

                if (input != null) {
                    properties.load(input);
                } else {
                    throw new RuntimeException("Unable to load application.properties");
                }
            }catch(Exception e){

            }

            dbURL = properties.getProperty("spring.datasource.url");
            dbUser = properties.getProperty("spring.datasource.username");
            dbPass = properties.getProperty("spring.datasource.password");
            sleeperURL = properties.getProperty("sleeper.url");
    }
}
