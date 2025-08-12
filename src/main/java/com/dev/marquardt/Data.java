package com.dev.marquardt;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;

public class Data {
    private static String[][] teamList = new String[32][4];
    private static String csvFilePath = "RosterLinks.csv";
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

                    teamListUpdated = true;
                }
            } catch (Exception e) {
                System.out.println("Error creating team objects:  " + e.getMessage());
            }
        }

        boolean teamRostersUpdated = false;
        if(teamListUpdated) {

        }

        return dataUpdated;
    }
}
