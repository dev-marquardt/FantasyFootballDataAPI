package com.dev.marquardt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

public class Team {
    private String teamName = null;
    private String abvTeamName = null;
    private int year;
    private String rosterLink = null;
    private ArrayList<Player> playerList = new ArrayList<Player>();

    public Team(){
        teamName = "";
        abvTeamName = "";
        year = 0;
        rosterLink = "";
    }

    public Team(final String teamNameInput, final String abvTeamNameInput, final int yearInput, final String rosterLinkInput){
        teamName = teamNameInput;
        abvTeamName = abvTeamNameInput;
        year = yearInput;
        rosterLink = rosterLinkInput;
    }

    public void updateRosters() throws RuntimeException{
        try{
            Document doc = Jsoup.connect(rosterLink).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36").get();


        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public String getTeam() {
        return teamName;
    }

    public String getAbvTeamName() {
        return abvTeamName;
    }

    public int getYear() {
        return year;
    }

    public String getRosterLink() {
        return rosterLink;
    }

    public ArrayList<Player> getPlayerList() {
        return playerList;
    }

    public void setTeam(final String teamNameInput) {
        teamName = teamNameInput;
    }

    public void setAbvTeamName(final String abvTeamNameInput) {
        abvTeamName = abvTeamNameInput;
    }

    public void setYear(final int yearInput) {
        year = yearInput;
    }

    public void setRosterLink(final String rosterLinkInput) {
        rosterLink = rosterLinkInput;
    }
}
