package com.dev.marquardt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.time.*;

public class Data {

    private static String dateUpdated = null;

    private static Connection conn = null;

    public static void init() throws RuntimeException {

        if(conn == null) {
            try {
                conn = DriverManager.getConnection(Settings.dbURL, Settings.dbUser, Settings.dbPass);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // only run once per day due to size of database, but this to update the initil database
    protected static boolean updatePlayerDatabase() throws RuntimeException {

        boolean output = false;

        StringBuilder sb = new StringBuilder();

        Map<Integer, Object> jsonPlayerData = new HashMap<Integer, Object>();

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


                    jsonPlayerData.put(playerID, playerData.toMap());

                    if(playerID > maxPlayerId){
                        maxPlayerId = playerID;
                    }

                }
            }

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
                                "injuryNotes CLOB," +
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
                                "injuryStatus VARCHAR(255))"
                );

            try{
                conn.createStatement().execute(createTable);

            }catch(Exception e){
                throw new RuntimeException(e);
            }

            if(maxPlayerId != -1){
                Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

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

        dateUpdated = Long.toString(Instant.now().getEpochSecond());

        return output;
    }

    // this is a backup function, use playerID for main usage
    protected static JSONObject getPlayer(int playerId) throws RuntimeException{

        JSONObject jsonPlayerData = null;
        Map<String, Object> playerData = new HashMap<>();

        String query = "SELECT * FROM nflPlayerData WHERE searchFullName = ?";

        try{
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, playerId);
            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                ResultSetMetaData metaData = rs.getMetaData();

                int columnCount = metaData.getColumnCount();

                for(int i = 1; i <= columnCount; i++){
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    playerData.put(columnName, value);
                }

                if(rs.next()){
                    throw new RuntimeException("Players with duplicate ID's");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        jsonPlayerData = new  JSONObject(playerData);

        return jsonPlayerData;
    }

    public static String getDateUpdated() {
        return dateUpdated;
    }
}
