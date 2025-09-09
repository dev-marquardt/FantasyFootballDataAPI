package com.dev.marquardt;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
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

    public Team() {
        teamName = "";
        abvTeamName = "";
        year = 0;
        rosterLink = "";
    }

    public Team(final String teamNameInput, final String abvTeamNameInput, final int yearInput, final String rosterLinkInput) {
        teamName = teamNameInput;
        abvTeamName = abvTeamNameInput;
        year = yearInput;
        rosterLink = rosterLinkInput;
    }

    public boolean updateRosters() throws RuntimeException {
        boolean rostersUpdated = false;

        if (!playerList.isEmpty()) {
            playerList.clear();
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(rosterLink);
            request.addHeader("User-Agent", "Email: dev.marquardt@gmail.com | Script: MyNFLScraper/1.0");

            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                Document doc = null;

                if (entity != null) {
                    String html = EntityUtils.toString(entity);
                    EntityUtils.consume(entity);

                    String start = "<!--";
                    String end = "-->";
                    int startIndex = html.indexOf(start);

                    while (startIndex != -1) {
                        int endIndex = html.indexOf(end, startIndex + start.length());

                        if (endIndex != -1) {
                            String comment = html.substring(startIndex + start.length(), endIndex).trim();

                            if (comment.contains("id=\"roster\"")) {
                                doc = Jsoup.parse(comment);
                                break;
                            }

                            startIndex = html.indexOf(start, endIndex + end.length());
                        } else {
                            break;
                        }
                    }
                }

                if (doc == null) {
                    System.out.println("No roster table found");
                    return false;
                }

                for (Element row : doc.select("table#roster tbody tr")) {
                    List<String> rowData = new ArrayList<String>();

                    for (Element cell : row.select("th, td")) {
                        rowData.add(cell.text().trim());
                    }

                    if (rowData.size() < 13) {
                        continue;
                    }

                    String[] playerNameParts = rowData.get(1).split(" ");
                    String firstName = playerNameParts.length > 0 ? playerNameParts[0] : "";
                    String lastName = playerNameParts.length > 1 ? playerNameParts[1] : "";

                    int draftYear = -1;
                    int seasonsPlayed = -1;

                    String exp = rowData.get(10).toUpperCase();

                    if (exp.equals("ROOK") || exp.equals("R") || exp.isEmpty()) {
                        draftYear = this.year;
                        seasonsPlayed = 0;
                    } else if (!exp.isEmpty()) {
                        try {
                            seasonsPlayed = Integer.parseInt(exp);
                            if (seasonsPlayed > 0) {
                                String drafted = rowData.get(12);
                                if (!drafted.isEmpty() && drafted.contains("/")) {
                                    String[] draftParts = drafted.split("/");
                                    if (draftParts.length >= 4) {
                                        String yearStr = draftParts[draftParts.length - 1].trim().replaceAll("\\D.*", "");
                                        draftYear = Integer.parseInt(yearStr);
                                    }
                                }
                                if (draftYear == -1) {
                                    draftYear = this.year - seasonsPlayed;
                                }
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid
                        }
                    }

                    if (draftYear == -1 || seasonsPlayed == -1) {
                        System.err.println("Skipping player with invalid draft/exp data: " + rowData.get(1));
                        continue;
                    }

                    playerList.add(new Player(rowData.get(0), firstName, lastName, this.abvTeamName, rowData.get(3), rowData.get(2).isEmpty() ? 0 : Integer.parseInt(rowData.get(2)), draftYear, seasonsPlayed));
                }

                rostersUpdated = true;
            }
        } catch (Exception e) {
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