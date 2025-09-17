package com.dev.marquardt;

import java.util.Scanner;
import org.json.JSONObject;

public class Main {
    public static void main(String[] args) {
        Data.init();

        Scanner sc = new Scanner(System.in);

        String input = "";

        System.out.println("exit() to exit");

        while (!input.equals("exit()")){

            System.out.println("Choose a player ID:");

        input = sc.nextLine();

        if(input.equals("exit()")){
            break;
        }

        try {
            int inputId = Integer.parseInt(input);

            if (inputId > -1) {
                JSONObject selectedPlayer = Data.getPlayer(inputId);

                System.out.println(selectedPlayer);
            }
        } catch (Exception e) {
            System.out.println("Invalid input");

            e.printStackTrace();
        }
    }
    }
}