[![N|Solid](https://pbs.twimg.com/profile_images/1248589572730044423/bdT7f7ig.jpg)](https://github.com/zch93/footballAPI)
# What was behind the performance of the Hungarian national football team on the European Cup Qualification?

This is the question what was in my mind, and the data can provide the answer, as always.
So this was the goal of my project, to gather and analyze data about our national football team.
# The Pipeline

  - Available and callable open API
  - Java based program to build up json REST API calls
  - Java based program to push the gathered data into a database
  - MySQL database to write queries
  - BI tool to represent data



## Build Java Code

To build up my Java project I used Maven clean install. The basic *pom.xml* file contained all the necessary initial information, however I had to write the following dependencies and properties to build my Java project.

```html
  <dependencies>
        <dependency>
            <groupId>com.googlecode.json-simple</groupId>
            <artifactId>json-simple</artifactId>
            <version>1.1</version>
        </dependency>
        <dependency>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
        </dependency>
        <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>5.1.46</version>
        </dependency>
    </dependencies>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <property name="hibernate.connection.url">
            jdbc:mysql://127.0.0.1/database?autoReconnect=true
        </property>
        <property name="connection.provider_class">org.hibernate.connection.C3P0ConnectionProvider</property>
        <property name="c3p0.acquire_increment">1</property>
        <property name="c3p0.idle_test_period">100</property> <!-- seconds -->
        <property name="c3p0.max_size">100</property>
        <property name="c3p0.max_statements">0</property>
        <property name="c3p0.min_size">10</property>
        <property name="c3p0.timeout">1800</property> <!-- seconds -->
    </properties>
```
    
  ## About the API
I've called two parts of the structure:
- Fixture: Give back a JSONArray with basic information about the match (eg. scored goals, result in the first and second hald etc.)
Structure of Fixtures:
```java
"response": [
{
    "fixture": {},
    "league": {},
    "teams": {},
    "goals": {},
    "score": {}
 ...}
]
```
- Statistics: Give back a JSONArray which contains detailed statistics about the match (eg. shots on goal, shots off goal, total shots etc.)

Structure of Statistics:
```java
"response": [
{
    "team": {},
    "statistics": [
        {
            "type": "xxx",
            "value": x
     ...}
 ```
 
## Java part
There are two important classes in my project:
1) To get response about the fixtures
```java
public class GetInputFixtureData_WORK {...}
```
2) To obtain data about detailed statistics related to a particular fixture
```java
public class GetFixtureStatistics {...}
```

I used an HttpRequest instance to make GET request, to build up response body as a String
```java
HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://v3.football.api-sports.io/fixtures?league=4&season=2020"))
                .header("x-rapidapi-host", "v3.football.api-sports.io")
                .header("x-rapidapi-key", "cefab1bbf6938384f876437d5c3cd9be")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

HttpResponse<String> response;
```

The important part of my classes is the way how I gathered the data from the response message.
1) In case of the Fixtures, it was easier and I wrote a "for cycle" to iterate through the response JSONArray:
```java
JSONArray arr = (JSONArray) rest.get("response");

for (int i = 0; i < arr.size(); i++) {
    JSONObject fixture = (JSONObject) arr.get(i);

    //response -> fixture (id, referee, timezone, date)
    JSONObject fix = (JSONObject) fixture.get("fixture");
    long fixtureID = (long) fix.get("id");
    String referee = (String) fix.get("referee");
    String timeZone = (String) fix.get("timezone"); ....etc.
```

2) In case of the Statistics part, it was a bigger challange, because the response JSONArray contains other 2 JSONArrays about the statistics related to the two teams. Furthermore inside the statistics JSONArray, there are alway 2 fields: type and value, and the value field can contains integer and String as well. So first I should built up a "for cycle" inside an other "for cycle", then I ad to solve the value "problem" with a generic class (StatValueGen()):
```java
JSONArray arr = (JSONArray) rest.get("response");
for (int i = 0; i < arr.size(); i++) {
    JSONObject stat = (JSONObject) arr.get(i);
    int statFixtureID = (int) fixtureID;

    //response -> fixture (team id, team name)
    JSONObject fix = (JSONObject) stat.get("team");
    long teamID = (long) fix.get("id"); ... etc.
    
    JSONArray statArr = (JSONArray) stat.get("statistics");
        for (int j = 0; j < statArr.size(); j++) {
          SONObject statistics = (JSONObject) statArr.get(j);
          String type = (String) statistics.get("type");
    
          StatValueGen value = new StatValueGen();
          value.addElement(statistics.get("value")); ...etc.
          
       ...}
       
  ...}
```
#
At the end of the programs two main steps executed:
- Defining a MySQL query to insert data into the database
- Execute the insert into update on the database

1) MySQL query (one example with the fixtures)
```java
String query = "INSERT INTO fixtures (" +
        "fixture_id, referee, time_zone, time_stamp, league_id,"+
        "league_round, home_team_id, home_team_name, away_team_id, away_team_name," +
        "goals_home, goals_away,score_halftime_home, score_halftime_away,  score_fulltime_home, score_fulltime_away, score_extratime_home, score_extratime_away)" +
        " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
```
2) Execute the update with the implemtation of PreparedStatement interface (example)
```java
PreparedStatement pstmt = conDB.prepareStatement(query);
    pstmt.setLong(1, fixtureID);
    pstmt.setString(2,referee);
    pstmt.setString(3, timeZone);
    pstmt.setString(4,timestampString);
    ...etc.
    pstmt.executeUpdate();
```

## MySQL part
In database level I was interested about the Hungarian team related statistics, so I wrote queries to get information about the results of the team.
#
Examples:
1) Query to get all matches what the Hun team had, the opponent, the goals what the Hun team scored or got and also the final result as they were the WINNER or that was a matxh which had been LOST:

```sql
SELECT 
    fixture_id,
    league_round,
    home_team_name,
    goals_home,
    goals_away,
    away_team_name,
    CASE
        WHEN
            home_team_name = 'Hungary'
                AND goals_home > goals_away THEN 'WINNER'
        WHEN
            away_team_name = 'Hungary'
                AND goals_away > goals_home THEN 'WINNER'
        WHEN fixture_id > 657683 THEN 'FUTURE MATCH'
        ELSE IF(goals_home = goals_away, 'DRAW', 'LOST')
    END AS hun_wins
FROM
    fixtures
WHERE
    home_team_name = 'Hungary'
        OR away_team_name = 'Hungary'
ORDER BY fixture_id;
```
![alt text](https://github.com/zch93/footballAPI_pics/blob/main/Query_1.png?raw=true)
#
2) I wrote an other query to get data about the shots and goals per match
```sql
SELECT
    s.fixture_id,
    s.team_name,
    s.shots_on_goal,
    s.shots_off_goal,
    s.blocked_shots,
    IF (s.team_name = f.home_team_name, f.away_team_name, f.home_team_name) as opponent,
    CASE
        WHEN s.team_name = f.home_team_name THEN f.goals_home
        WHEN s.team_name = f.away_team_name THEN f.goals_away
        ELSE 'null'
    END AS goals_on_match
FROM
    statistics s
        JOIN fixtures f ON s.fixture_id = f.fixture_id
HAVING s.team_name = "Hungary";
```
![alt text](https://github.com/zch93/footballAPI_pics/blob/main/Query_2.png?raw=true)

(There will be other queries as well, as this part of the project is under construction)
#
And in the last step I imported the results into Tableau as .CSV files.

## Tableau Public (BI) part

At the end of the pipeline of my project I worked with Tableaue as a BI tool and I just started to prepare insightful charts based on the data fetched from the API.

![alt text](https://github.com/zch93/footballAPI_pics/blob/main/example.png?raw=true)

Possible insights can be:
- it shows in the performance that when we “don’t get a word,” so we can only manage from relatively few shots, we can’t score a goal. So we usually use our opportunities poorly. Or we usually have fewer 'shots on goal', so if we can attack an opponent frequently, we can only predict a goal after a certain minimum number of shots (since in case of the Hun team, a large number of shots do not generate many goals, based on the data)
- so, importantly, we can reduce the size of the Gray area ('shots off goal') by improving the shot accuracy. And the red part ('blocked shots') is only partly up to us. To reduce the size of red part, technique, tactics and shooting situation creation need to be developed. For example, due to a poor tactic, the situation is not good enough and the players may shoot too fast, too far, from too bad a position, and shots may be blocked.
