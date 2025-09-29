package com.dev.marquardt;

import java.util.Properties;

public class Settings {


    private static final Properties properties = new  Properties();

    protected static final String dbURL;

    protected static final String dbUser;

    protected static final String dbPass;

    protected static final String sleeperURL;

    protected static final String[] allPFRSeasonStatsURL;

    protected static final String currSeason;

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

            currSeason = properties.getProperty("nfl.currentseason");

            allPFRSeasonStatsURL = new String[]{
                    String.format(properties.getProperty("pfr.seasonpassing.url"), currSeason),
                    String.format(properties.getProperty("pfr.seasonscrimmage.url"),currSeason),
                    String.format(properties.getProperty("pfr.seasondefense.url"),currSeason),
                    String.format(properties.getProperty("pfr.advancedseasonpassing.url"),currSeason),
                    String.format(properties.getProperty("pfr.advancedseasonrushing.url"),currSeason),
                    String.format(properties.getProperty("pfr.advancedseasonreceiving.url"),currSeason),
                    String.format(properties.getProperty("pfr.advancedseasondefense.url"),currSeason),
                    String.format(properties.getProperty("pfr.advancedteamstats.url"),currSeason),
                    String.format(properties.getProperty("pfr.fantasyplayerranks.url"),currSeason),
                    String.format(properties.getProperty("pfr.redzonepassing.url"),currSeason),
                    String.format(properties.getProperty("pfr.redzonerushing.url"),currSeason),
                    String.format(properties.getProperty("pfr.redzonereceiving.url"), currSeason)
            };
    }
}
