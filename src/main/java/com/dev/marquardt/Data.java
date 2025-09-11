package com.dev.marquardt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Data {

    // only run once per day due to size of database, but this to update the initil database
    protected static boolean updatePlayerDatabase() throws RuntimeException {

        boolean output = false;

        StringBuilder sb = new StringBuilder();

        Map<Integer, JSONObject> jsonPlayerData = new HashMap<Integer, JSONObject>();

        Integer maxPlayerId = -1;

        try {
            URL url = new URL(Settings.sleeperURL);

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();

            JSONObject jsonObject = new JSONObject(sb.toString());

            for(String key :  jsonObject.keySet()) {
                if(key.matches("\\d+")){
                    int playerID = Integer.parseInt(key);

                    JSONObject playerData = jsonObject.getJSONObject(key);

                        jsonPlayerData.put(playerID, playerData);

                        if(playerID > maxPlayerId){
                            maxPlayerId = playerID;
                        }

                        playerData.remove("kalshi_id");
                        playerData.remove("rotoworld_id");
                        playerData.remove("competitions");
                        playerData.remove("opta_id");
                        playerData.remove("team_changed_at");
                        playerData.remove("fantasy_data_id");
                        playerData.remove("swish_id");
                        playerData.remove("gsis_id");
                        playerData.remove("oddsjam_id");
                        playerData.remove("stats_id");
                        playerData.remove("hashtag");
                        playerData.remove("metadata");
                        playerData.remove("rotowire_id");
                        playerData.remove("pandascore_id");
                        playerData.remove("team_abbr");
                        playerData.remove("fantasy_positions");
                        playerData.remove("player_id");

                }
            }

            try{
                Connection  conn = DriverManager.getConnection(Settings.dbURL, Settings.dbUser, Settings.dbPass);

                final String createTable = new String(
                        "CREATE TABLE IF NOT EXISTS nflPlayerData (" +
                                "playerID INT UNIQUE NOT NULL PRIMARY KEY," +
                                "firstName VARCHAR(255) NOT NULL," +
                                "birthState VARCHAR(255)," +
                                "depthChartPosition VARCHAR(50)," +
                                "team VARCHAR(50)," +
                                "age INT," +
                                "active BOOLEAN," +
                                "depthChartOrder INT," +
                                "weight INT," +
                                "college VARCHAR(255)," +
                                "birthCity VARCHAR(255)," +
                                "injuryNotes VARCHAR(MAX)," +
                                "birthDate VARCHAR(50)," +
                                "status VARCHAR(50)," +
                                "sport VARCHAR(50)," +
                                "newsUpdated BIGINT," +
                                "height VARCHAR(10)," +
                                "birthCountry VARCHAR(100)," +
                                "yearsExp INT," +
                                "searchFirstName VARCHAR(255)," +
                                "number INT," +
                                "injuryBodyPart VARCHAR(255)," +
                                "practiceDesc VARCHAR(255)," +
                                "practicePart VARCHAR(255)," +
                                "injuryStartDate VARCHAR(50)," +
                                "searchRank INT," +
                                "espnId INT," +
                                "sportsRadarId VARCHAR(255)," +
                                "yahooId INT," +
                                "position VARCHAR(10)," +
                                "highSchool VARCHAR(255)," +
                                "searchFullName VARCHAR(255)," +
                                "searchLastName VARCHAR(255)," +
                                "lastName VARCHAR(255) NOT NULL," +
                                "fullName VARCHAR(255) NOT NULL," +
                                "INDEX idxFullName (fullName)," +
                                "INDEX idxTeam (team)," +
                                "injuryStatus VARCHAR(255))"
                );

                conn.createStatement().execute(createTable);

            }catch(Exception e){
                throw new RuntimeException(e);
            }

            if(maxPlayerId == -1){
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                try(FileWriter fw = new FileWriter("playerData.json")){
                    gson.toJson(jsonPlayerData, fw);

                    fw.close();

                    output = true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch(Exception e){
            throw new RuntimeException(e);
        }

        if(maxPlayerId == -1){
            throw new RuntimeException("Error reading playerData.json");
        }

        return output;
    }
}
