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

            while(reader.readNext() != null){
                row = reader.readNext();

                if(i > 31){
                    break;
                }

                teamList[i] = row;

                i++;
            }

            csvUpdated = true;

        }catch(Exception e){
            System.out.println("Error reading csv: " + e.getMessage());
        }

        boolean teamListUpdated = false;
        if(csvUpdated) {
            try {
                int i = 0;

                for (String[] row : teamList) {
                    if (i > 31) {
                        break;
                    }

                    Team temp = new Team(row[0], row[1], Integer.parseInt(row[2]), row[3]);

                    teams[i] = temp;

                    i++;
                }
            } catch (Exception e) {
                System.out.println("Error creating team objects:  " + e.getMessage());
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

                    boolean rostertsUpdated = team.updateRosters();

                    if(!rostertsUpdated){
                        System.out.println("Roster not updated: " + team.getTeam());
                    }

                }catch(Exception e){
                    System.out.println("Error updating team roster: " + team.getTeam() + e.getMessage());
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
            }

            exportPlayerData = true;
        }

        return dataUpdated;
    }
}
