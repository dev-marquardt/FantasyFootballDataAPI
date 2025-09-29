package com.dev.marquardt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

// imports for web scraping
import com.google.common.util.concurrent.RateLimiter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.*;

public class Data {

    private ArrayList<String> seasonDataFileNames = new ArrayList<String>();

    private String dateUpdated = null;

    private Connection conn = null;

    private RateLimiter rl = null;

    public void init() throws RuntimeException {

        final double rateLimit = (20.0 / 60.0); // 20 calls per minute
        rl = RateLimiter.create(rateLimit);

        if (conn == null) {
            try {
                conn = DriverManager.getConnection(Settings.dbURL, Settings.dbUser, Settings.dbPass);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // only run once per day due to size of database, but this to update the initil database
    protected boolean updatePlayerDatabase() throws RuntimeException {

        boolean output = false;

        StringBuilder sb = new StringBuilder();

        Map<Integer, Object> jsonPlayerData = new HashMap<Integer, Object>();
        Map<Integer, JSONObject> sqlPlayerData = new HashMap<Integer, JSONObject>();

        Integer maxPlayerId = -1;

        try {
            URL url = new URL(Settings.sleeperURL);

            rl.acquire();

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();

            JSONObject jsonObject = new JSONObject(sb.toString());

            for (String key : jsonObject.keySet()) {
                if (key.matches("\\d+")) {
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
                    sqlPlayerData.put(playerID, playerData);

                    if (playerID > maxPlayerId) {
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

            try {
                conn.createStatement().execute(createTable);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Specify columns for robustness (matches CREATE TABLE order)
            String insertSQL = "MERGE INTO nflPlayerData (playerID, firstName, birthState, depthChartPosition, team, age, active, depthChartOrder, weight, college, birthCity, injuryNotes, birthDate, status, sport, newsUpdated, height, birthCountry, yearsExp, searchFirstName, number, injuryBodyPart, practiceDesc, practicePart, injuryStartDate, searchRank, espnId, sportsRadarId, yahooId, position, highSchool, searchFullName, searchLastName, lastName, fullName, injuryStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = conn.prepareStatement(insertSQL)) {

                for (Map.Entry<Integer, JSONObject> entry : sqlPlayerData.entrySet()) {
                    JSONObject playerData = entry.getValue();
                    int playerID = entry.getKey();

                    statement.setInt(1, playerID);
                    statement.setString(2, playerData.optString("first_name", "Unknown")); // firstName
                    statement.setString(3, playerData.optString("birth_state", null)); // birthState
                    statement.setString(4, playerData.optString("depth_chart_position", null)); // depthChartPosition
                    statement.setString(5, playerData.optString("team", null)); // team (no change)
                    statement.setInt(6, playerData.optInt("age", 0)); // age (no change)
                    statement.setBoolean(7, playerData.optBoolean("active", false)); // active (no change)
                    statement.setInt(8, playerData.optInt("depth_chart_order", 0)); // depthChartOrder
                    statement.setInt(9, playerData.optInt("weight", 0)); // weight (no change)
                    statement.setString(10, playerData.optString("college", null)); // college (no change)
                    statement.setString(11, playerData.optString("birth_city", null)); // birthCity
                    statement.setString(12, playerData.optString("injury_notes", null)); // injuryNotes
                    statement.setString(13, playerData.optString("birth_date", null)); // birthDate (assuming snake_case; adjust if "birthDate")
                    statement.setString(14, playerData.optString("status", null)); // status (no change)
                    statement.setString(15, playerData.optString("sport", null)); // sport (no change)
                    statement.setLong(16, playerData.optLong("news_updated", 0L)); // newsUpdated
                    statement.setString(17, playerData.optString("height", null)); // height (no change)
                    statement.setString(18, playerData.optString("birth_country", null)); // birthCountry
                    statement.setInt(19, playerData.optInt("years_exp", 0)); // yearsExp
                    statement.setString(20, playerData.optString("search_first_name", null)); // searchFirstName
                    statement.setInt(21, playerData.optInt("number", 0)); // number (no change)
                    statement.setString(22, playerData.optString("injury_body_part", null)); // injuryBodyPart
                    statement.setString(23, playerData.optString("practice_desc", null)); // practiceDesc
                    statement.setString(24, playerData.optString("practice_part", null)); // practicePart
                    statement.setString(25, playerData.optString("injury_start_date", null)); // injuryStartDate
                    statement.setInt(26, playerData.optInt("search_rank", 0)); // searchRank
                    statement.setInt(27, playerData.optInt("espn_id", 0)); // espnId
                    statement.setString(28, playerData.optString("sports_radar_id", null)); // sportsRadarId
                    statement.setInt(29, playerData.optInt("yahoo_id", 0)); // yahooId
                    statement.setString(30, playerData.optString("position", null)); // position (no change)
                    statement.setString(31, playerData.optString("high_school", null)); // highSchool
                    statement.setString(32, playerData.optString("search_full_name", null)); // searchFullName
                    statement.setString(33, playerData.optString("search_last_name", null)); // searchLastName
                    statement.setString(34, playerData.optString("last_name", "Unknown")); // lastName
                    statement.setString(35, playerData.optString("full_name", "Unknown")); // fullName
                    statement.setString(36, playerData.optString("injury_status", null)); // injuryStatus

                    statement.addBatch();  // Batch for efficiency
                }

                statement.executeBatch();  // Execute all at once
            } catch (SQLException e) {
                throw new RuntimeException("Error inserting players", e);
            }

            if (maxPlayerId != -1) {
                Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

                try (FileWriter fw = new FileWriter("playerData.json")) {
                    gson.toJson(jsonPlayerData, fw);

                    fw.close();

                    output = true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (maxPlayerId == -1) {
            throw new RuntimeException("Error reading playerData.json");
        }

        dateUpdated = Long.toString(Instant.now().getEpochSecond());

        return output;
    }

    // this is a backup function, use playerID for main usage
    protected JSONObject getPlayer(int playerId) throws RuntimeException {

        JSONObject jsonPlayerData = null;
        Map<String, Object> playerData = new HashMap<>();

        String query = "SELECT * FROM nflPlayerData WHERE playerID = ?";

        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, playerId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                ResultSetMetaData metaData = rs.getMetaData();

                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    playerData.put(columnName, value);
                }

                if (rs.next()) {
                    throw new RuntimeException("Players with duplicate ID's");
                }

                return new JSONObject(playerData);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    //using web scraping to just grab the total stats for a player then will have different function for the indivual week
    protected void updatePlayerStats() throws RuntimeException {
        final String outputFileName = "%s.json";  // Dynamic title + .json; year already in title

        for (String url : Settings.allPFRSeasonStatsURL) {
            try {
                rl.acquire();
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36")
                        .timeout(1000)
                        .get();

                String fileTitle = doc.title().split("\\|")[0].trim().replaceAll("\\s+", "_");

                Element table = doc.selectFirst("table");
                if (table == null) {
                    throw new RuntimeException("Table not found URL: " + url);
                }

                // Fix: Select only the last row in thead for actual column headers
                Elements header = table.select("thead tr:last-child th");
                if (header.isEmpty()) {
                    throw new RuntimeException("Headers not found in table, URL: " + url);
                }

                ArrayList<String> tableHeaders = new ArrayList<>();
                for (Element th : header) {
                    tableHeaders.add(th.text().trim());
                }

                ArrayList<ArrayList<String>> tableData = new ArrayList<>();
                Elements dataRows = table.select("tbody tr:not(.thead)");
                for (Element row : dataRows) {
                    ArrayList<String> rowData = new ArrayList<>();  // Fix: New list per row
                    Elements cells = row.select("th, td");
                    for (Element cell : cells) {
                        String info = cell.text().trim();
                        rowData.add(info.isEmpty() ? "" : info);
                    }
                    if (!rowData.isEmpty()) {  // Skip empty rows
                        tableData.add(rowData);
                    }
                }

                Map<Integer, Object> jsonMap = new HashMap<>();
                int id = 0;
                for (ArrayList<String> row : tableData) {
                    Map<String, Object> dataMap = new HashMap<>();
                    for (int i = 0; i < tableHeaders.size() && i < row.size(); i++) {
                        dataMap.put(tableHeaders.get(i), row.get(i));
                    }
                    jsonMap.put(id, dataMap);
                    id++;
                }

                Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                try (FileWriter fw = new FileWriter(String.format(outputFileName, fileTitle))) {
                    gson.toJson(jsonMap, fw);

                    seasonDataFileNames.add(String.format(outputFileName, fileTitle));
                }

                jsonMap.clear();  // Clear for next URL
            } catch (Exception e) {
                System.err.println("Error processing URL: " + url + " - " + e.getMessage());
                // Continue to next URL instead of throwing
            }
        }

        // next step to get data into database then link using the same ID as the player Data Base.

        // read in the json data that was written (json data will stay as archive)
        StringBuilder sb = new StringBuilder();

        BufferedReader reader = null;

        try{
        for(String seasonDataFileName : seasonDataFileNames) {
            reader = new BufferedReader(new FileReader(seasonDataFileName));

            String line;

            while((line = reader.readLine()) != null){
                sb.append(line);
            }

            reader.close();

            JSONObject jsonObject = new JSONObject(sb.toString());

            String sqlQuery = "SELECT playerID FROM nflPlayerData Where fullName = ?";
            try{
                PreparedStatement statement = conn.prepareStatement(sqlQuery);

                System.out.println("\n");
                System.out.println(seasonDataFileName);

            for(String key : jsonObject.keySet()) {
                JSONObject data = jsonObject.getJSONObject(key);

                String playerName = data.get("Player").toString();

                statement.setString(1, playerName);

                ResultSet rs = statement.executeQuery();

                if(rs.next()) {
                    int playerID = rs.getInt("playerID");

                    System.out.println("player name: " + playerName + "; player ID: " + playerID);
                }
            }
            }catch(Exception e){
                throw new RuntimeException(e);
            }

        }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    protected String getDateUpdated() {

        return dateUpdated;
    }
}
