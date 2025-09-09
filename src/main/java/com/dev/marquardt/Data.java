package com.dev.marquardt;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.google.common.util.concurrent.RateLimiter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;

public class Data {
    private static String[][] teamList = new String[32][4];
    private static final String csvFilePath = "RosterLinks.csv";
    private static Team[] teams = new Team[32];

    public static boolean updateData(){
        boolean dataUpdated = false;

        boolean csvUpdated = false;
        try{
            CSVReader reader = new CSVReader(new FileReader(csvFilePath));

            String[] row;
            int i = 0;

            while((row = reader.readNext()) != null){

                if(i > 31){
                    break;
                }

                teamList[i] = row;

                for(String item : row){
                    System.out.println(item);
                }


                i++;
            }

            csvUpdated = true;

        }catch(Exception e){
            System.out.println("Error reading csv: " + e.getMessage());
            e.printStackTrace();
        }

        boolean teamListUpdated = false;
        if(csvUpdated) {
            try {
                int i = 0;

                for (String[] row : teamList) {
                    if (i > 31) {
                        break;
                    }

                    Team temp = new Team(row[0], row[1], 2025, row[3]);

                    System.out.println(row[0] + "\n" + row[1] + "\n" + row[2] + "\n" +row[3]);

                    teams[i] = temp;

                    i++;
                }
            } catch (Exception e) {
                System.out.println("Error creating team objects:  " + e.getMessage());
                e.printStackTrace();
            }

            teamListUpdated = true;
        }

        boolean teamRostersUpdated = false;
        if(teamListUpdated) {
            final double rateLimit = (20.0/60.0);

            final RateLimiter rateLimiter = RateLimiter.create(rateLimit);

            for(Team team : teams){
                try {
                    rateLimiter.acquire();

                    boolean rostersUpdated = team.updateRosters();

                    if(!rostersUpdated){
                        System.out.println("Roster not updated: " + team.getTeam());
                    }

                }catch(Exception e){
                    System.out.println("Error updating team roster: " + team.getTeam() + e.getMessage());
                    e.printStackTrace();
                }
            }

            teamRostersUpdated = true;
        }

        boolean exportPlayerData = false;
        if(teamRostersUpdated) {
            List<Player> players = new ArrayList<Player>();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            try(FileWriter writer = new FileWriter("players.json")){
                for(Team team : teams){
                    gson.toJson(team.playerList, writer);
                }
            }catch(Exception e){
                System.out.println("Error creating players file: " + e.getMessage());
                e.printStackTrace();
            }

            exportPlayerData = true;
        }

        return dataUpdated;
    }
}
