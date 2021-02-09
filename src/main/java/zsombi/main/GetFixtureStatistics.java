package zsombi.main;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GetFixtureStatistics {

    public void getStat(long fixtureID) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://v3.football.api-sports.io/fixtures/statistics?fixture="+fixtureID))
                .header("x-rapidapi-host", "v3.football.api-sports.io")
                .header("x-rapidapi-key", "cefab1bbf6938384f876437d5c3cd9be")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response;

        try {

          Class.forName("com.mysql.jdbc.Driver");
            Connection conDB = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/Football_API?autoReconnect=true&useSSL=false",
                    "root","password");

            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JSONParser parser = new JSONParser();

            JSONObject rest = (JSONObject) parser.parse(response.body());

            JSONArray arr = (JSONArray) rest.get("response");


            for (int i = 0; i < arr.size(); i++) {
                JSONObject stat = (JSONObject) arr.get(i);

                int statFixtureID = (int) fixtureID;

                //response -> fixture (team id, team name)
                JSONObject fix = (JSONObject) stat.get("team");
                long teamID = (long) fix.get("id");
                String teamName = (String) fix.get("name");

                //response -> statistics
                JSONArray statArr = (JSONArray) stat.get("statistics");

                int shotsOnGoal = 0;
                int shotsOffGoal = 0;
                int totalShots = 0;
                int blockedShots = 0;
                int shotsInsideBox = 0;
                int shotsOutsidebox = 0;
                int fouls = 0;
                int cornerKicks = 0;
                int offSides = 0;
                int ballPossession = 0;
                int yellowCards = 0;
                int redCards = 0;
                int goalkeeperSaves = 0;
                int totalPasses = 0;
                int passesAccurate = 0;
                int passSuccess = 0;

                for (int j = 0; j < statArr.size(); j++) {
                    JSONObject statistics = (JSONObject) statArr.get(j);

                    String type = (String) statistics.get("type");

                    StatValueGen value = new StatValueGen();
                    value.addElement(statistics.get("value"));

                    if (statistics.get("type").equals("Shots on Goal")) {
                        if (statistics.get("value") == null) {
                            shotsOnGoal = 0;
                        } else {
                            shotsOnGoal = Integer.parseInt(String.valueOf(value));
                        }
                    }
                    else if (statistics.get("type").equals("Shots off Goal")) {
                        if (statistics.get("value") == null) {
                            shotsOffGoal = 0;
                        } else {
                            shotsOffGoal = Integer.parseInt(String.valueOf(value));
                        }
                    }
                    else if (statistics.get("type").equals("Total Shots")) {
                        if (statistics.get("value") == null) {
                            totalShots = 0;
                        } else {
                            totalShots = Integer.parseInt(String.valueOf(value));
                        }
                    }
                    else if (statistics.get("type").equals("Blocked Shots")) {
                        if (statistics.get("value") == null) {
                            blockedShots = 0;
                        } else {
                            blockedShots = Integer.parseInt(String.valueOf(value));
                        }
                    }
                    else if (statistics.get("type").equals("Shots insidebox")) {
                        if (statistics.get("value") == null) {
                            shotsInsideBox = 0;
                        } else {
                            shotsInsideBox = Integer.parseInt(String.valueOf(value));
                        }
                    }
                    else if (statistics.get("type").equals("Shots outsidebox")) {
                        if (statistics.get("value") == null) {
                            shotsOutsidebox = 0;
                        } else {
                            shotsOutsidebox = Integer.parseInt(String.valueOf(value));
                        }
                    }
                    else if (statistics.get("type").equals("Fouls")) {
                        if (statistics.get("value") == null) {
                            fouls = 0;
                        } else {
                            fouls = Integer.parseInt(String.valueOf(value));
                        }
                    }
                    else if (statistics.get("type").equals("Corner Kicks")) {
                        if (statistics.get("value") == null) {
                            cornerKicks = 0;
                        } else {
                            cornerKicks = Integer.parseInt(String.valueOf(value));
                        }
                    }
                    else if (statistics.get("type").equals("Offsides")) {
                        if (statistics.get("value") == null) {
                            offSides = 0;
                        } else {
                            offSides = Integer.parseInt(String.valueOf(value));
                        }
                    }
                    else if (statistics.get("type").equals("Ball Possession")) {
                        String possessionToInt = String.valueOf(value);
                        StringToInt possToInt = new StringToInt();
                        ballPossession = possToInt.generateInt(possessionToInt);
                    }
                    else if (statistics.get("type").equals("Yellow Cards")) {
                        if (statistics.get("value") == null) {
                            yellowCards = 0;
                        } else {
                            yellowCards = Integer.parseInt(String.valueOf(value));
                        }
                    }
                    else if (statistics.get("type").equals("Red Cards")) {
                        if (statistics.get("value") == null) {
                            redCards = 0;
                        } else {
                            redCards = Integer.parseInt(String.valueOf(value));
                        }
                    }
                    else if (statistics.get("type").equals("Goalkeeper Saves")) {
                        if (statistics.get("value") == null) {
                            goalkeeperSaves = 0;
                        } else {
                            goalkeeperSaves = Integer.parseInt(String.valueOf(value));
                        }
                    }
                    else if (statistics.get("type").equals("Total passes")) {
                        totalPasses = Integer.parseInt(String.valueOf(value));
                    }
                    else if (statistics.get("type").equals("Passes accurate")) {
                        passesAccurate = Integer.parseInt(String.valueOf(value));
                    }
                    else if (statistics.get("type").equals("Passes %")) {
                        String passToInt = String.valueOf(value);
                        StringToInt passSucToInt = new StringToInt();
                        passSuccess = passSucToInt.generateInt(passToInt);
                    }

                }

                String query = "INSERT INTO statistics (" +
                                "fixture_id, team_ID, team_name, shots_on_goal, shots_off_goal,"+
                                "total_shots, blocked_shots, shots_inside_box, shots_outside_box, fouls,"+
                                "corner_kicks, offsides, ball_possession_rate, yellow_cards, red_cards,"+
                                "goalkeeper_saves, total_passes, passes_accurate, pass_success_rate)" +
                                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement pstmt = conDB.prepareStatement(query);
                    pstmt.setInt(1, statFixtureID);
                    pstmt.setLong(2, teamID);
                    pstmt.setString(3, teamName);
                    pstmt.setInt(4, shotsOnGoal);
                    pstmt.setInt(5, shotsOffGoal);
                    pstmt.setInt(6, totalShots);
                    pstmt.setInt(7, blockedShots);
                    pstmt.setInt(8, shotsInsideBox);
                    pstmt.setInt(9, shotsOutsidebox);
                    pstmt.setInt(10, fouls);
                    pstmt.setInt(11, cornerKicks);
                    pstmt.setInt(12, offSides);
                    pstmt.setInt(13, ballPossession);
                    pstmt.setInt(14, yellowCards);
                    pstmt.setInt(15, redCards);
                    pstmt.setInt(16, goalkeeperSaves);
                    pstmt.setInt(17, totalPasses);
                    pstmt.setInt(18, passesAccurate);
                    pstmt.setInt(19, passSuccess);
                    pstmt.executeUpdate();

            }

            conDB.close();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }
}
