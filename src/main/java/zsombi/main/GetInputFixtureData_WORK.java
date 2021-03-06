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
import java.sql.*;
import java.text.SimpleDateFormat;

public class GetInputFixtureData_WORK {

    public static void main(String[] args) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://v3.football.api-sports.io/fixtures?league=4&season=2020"))
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
                JSONObject fixture = (JSONObject) arr.get(i);

                //response -> fixture (id, referee, timezone, date)
                JSONObject fix = (JSONObject) fixture.get("fixture");
                long fixtureID = (long) fix.get("id");
                String referee = (String) fix.get("referee");
                String timeZone = (String) fix.get("timezone");
                long timestamp = (long) fix.get("timestamp");
                String timestampString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                                                        .format(new Date(timestamp * 1000));

                //response -> league (id, round)
                JSONObject league = (JSONObject) fixture.get("league");
                long leagueID = (long) league.get("id");
                String round = (String) league.get("round");

                //response -> teams (home, away)
                JSONObject teams = (JSONObject) fixture.get("teams");
                JSONObject home = (JSONObject) teams.get("home");
                JSONObject away = (JSONObject) teams.get("away");
                long homeID = (long) home.get("id");
                String homeTeamName;
                long awayID = (long) away.get("id");
                String awayTeamName;

                if (home.get("name") == null) {
                    homeTeamName = "Not yet defined";
                } else {
                    homeTeamName = (String) home.get("name");
                }

                if (away.get("name") == null) {
                    awayTeamName = "Not yet defined";
                } else {
                    awayTeamName = (String) away.get("name");
                }

                //response -> goals (home, away)
                JSONObject goals = (JSONObject) fixture.get("goals");
                long homeGoal;
                long awayGoal;

                if (goals.get("home") == null) {
                    homeGoal = 0;
                } else {
                    homeGoal = (long) goals.get("home");
                }

                if (goals.get("away") == null) {
                    awayGoal = 0;
                } else {
                    awayGoal = (long) goals.get("away");
                }

                //response -> score (halftime, fulltime, extratime)
                JSONObject score = (JSONObject) fixture.get("score");
                JSONObject scHalfTime = (JSONObject) score.get("halftime");
                JSONObject scFullTime = (JSONObject) score.get("fulltime");
                JSONObject scExtraTime = (JSONObject) score.get("extratime");
                long HThomeGoal;
                long HTawayGoal;
                long FThomeGoal;
                long FTawayGoal;
                long EThomeGoal;
                long ETawayGoal;

                if (scHalfTime.get("home") == null) {
                    HThomeGoal = 0;
                } else {
                    HThomeGoal = (long) scHalfTime.get("home");
                }
                if (scHalfTime.get("away") == null) {
                    HTawayGoal = 0;
                } else {
                    HTawayGoal = (long) scHalfTime.get("away");
                }

                if (scFullTime.get("home") == null){
                    FThomeGoal = 0;
                } else {
                    FThomeGoal = (long) scFullTime.get("home");
                }

                if (scFullTime.get("away") == null){
                    FTawayGoal = 0;
                } else {
                    FTawayGoal = (long) scFullTime.get("away");
                }

                if (scExtraTime.get("home") == null) {
                    EThomeGoal = 0;
                } else {
                    EThomeGoal = (long) scExtraTime.get("home");
                }

                if (scExtraTime.get("away") == null) {
                    ETawayGoal = 0;
                } else {
                    ETawayGoal = (long) scExtraTime.get("away");
                }


                String query = "INSERT INTO fixtures (" +
                                "fixture_id, referee, time_zone, time_stamp, league_id,"+
                                "league_round, home_team_id, home_team_name, away_team_id, away_team_name," +
                                "goals_home, goals_away,"+
                                "score_halftime_home, score_halftime_away, score_fulltime_home,"+
                                "score_fulltime_away, score_extratime_home, score_extratime_away)" +
                                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = conDB.prepareStatement(query);
                pstmt.setLong(1, fixtureID);
                pstmt.setString(2,referee);
                pstmt.setString(3, timeZone);
                pstmt.setString(4,timestampString);
                pstmt.setLong(5,leagueID);
                pstmt.setString(6,round);
                pstmt.setLong(7, homeID);
                pstmt.setString(8,homeTeamName);
                pstmt.setLong(9, awayID);
                pstmt.setString(10,awayTeamName);
                pstmt.setLong(11, homeGoal);
                pstmt.setLong(12, awayGoal);
                pstmt.setLong(13, HThomeGoal);
                pstmt.setLong(14,HTawayGoal);
                pstmt.setLong(15,FThomeGoal);
                pstmt.setLong(16, FTawayGoal);
                pstmt.setLong(17, EThomeGoal);
                pstmt.setLong(18, ETawayGoal);
                pstmt.executeUpdate();

            }

            conDB.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}
