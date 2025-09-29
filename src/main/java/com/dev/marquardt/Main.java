package com.dev.marquardt;

public class Main {
    public static void main(String[] args) {

        try {
            Data dataHandler = new Data();

            dataHandler.init();

            dataHandler.updatePlayerDatabase();

            dataHandler.updatePlayerStats();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}