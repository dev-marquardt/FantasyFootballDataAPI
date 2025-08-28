package com.dev.marquardt;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String teamName = null;
    private String abvTeamName = null;
    private int year;
    private String rosterLink = null;
    public ArrayList<Player> playerList = new ArrayList<Player>();

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

    public boolean updateRosters() throws RuntimeException{
        boolean rostersUpdated = false;

        if (!playerList.isEmpty()){
            playerList.clear();
        }

        try{
            CloseableHttpClient client = HttpClients.createDefault();

            HttpGet request = new HttpGet(rosterLink);

            request.addHeader("User-Agent", "Email: dev.marquardt@gmail.com | Script: MyNFLScraper/1.0");

            String html = client.execute(request).getEntity().getContent().toString();

            String start = "<!--";
            String end = "-->";
            int startIndex = html.indexOf(start);

            Document doc = null;

            while(startIndex != -1){
                int endIndex = html.indexOf(end, startIndex +  start.length());

                if (endIndex != -1){
                    String comment = html.substring(startIndex +  start.length(), endIndex).trim();

                    if (comment.contains("id=roster")){
                        doc = Jsoup.parse(comment);
                        break;
                    }

                    startIndex = html.indexOf(start, endIndex + end.length());
                }
                else{
                    break;
                }
            }

            if (doc == null){
                System.out.println("No roster table found");
                return false;
            }

            for (Element row : doc.select("table#roster tbody tr")){
                List<String> rowData = new ArrayList<String>();

                for (Element cell : row.select("th, td")){
                    rowData.add(cell.text());
                }

                String[] playerName = rowData.get(1).split(" ");

                int draftYear = -1;
                int seasonsPlayed = -1;

                if(rowData.get(10).toUpperCase().equals("ROOK")){
                    draftYear = 2025;

                    seasonsPlayed = 0;
                }

                if(Integer.parseInt(rowData.get(10)) > 0 && !rowData.get(12).isEmpty()){
                    draftYear = Integer.parseInt(rowData.get(11).split("/")[3].trim());

                    seasonsPlayed = Integer.parseInt(rowData.get(10));
                }

                if(draftYear == -1 && seasonsPlayed == -1){
                    throw new RuntimeException("Draft year or seasons played is empty");
                }

                playerList.add(new Player(rowData.get(0), playerName[0], playerName[1], this.abvTeamName, rowData.get(3), Integer.parseInt(rowData.get(2)), draftYear,seasonsPlayed ));
            }

            rostersUpdated = true;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }

        return rostersUpdated;
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
