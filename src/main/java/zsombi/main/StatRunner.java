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

public class StatRunner {

    public static void main(String[] args) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://v3.football.api-sports.io/fixtures?league=4&season=2020"))
                .header("x-rapidapi-host", "v3.football.api-sports.io")
                .header("x-rapidapi-key", "cefab1bbf6938384f876437d5c3cd9be")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();


        HttpResponse<String> response;

        try {


            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JSONParser parser = new JSONParser();

            JSONObject rest = (JSONObject) parser.parse(response.body());

            JSONArray arr = (JSONArray) rest.get("response");


            for (int i = 0; i < arr.size(); i++) {
                JSONObject fixture = (JSONObject) arr.get(i);

                //response -> fixture (id, referee, timezone, date)
                JSONObject fix = (JSONObject) fixture.get("fixture");
                long fixtureID = (long) fix.get("id");

                GetFixtureStatistics stat = new GetFixtureStatistics();
                stat.getStat(fixtureID);

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
