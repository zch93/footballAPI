# footballAPI

[![N|Solid](https://pbs.twimg.com/profile_images/1248589572730044423/bdT7f7ig.jpg)](https://github.com/zch93/footballAPI)
## What was behind the performance of the Hungarian national football team on the European Cup Qualification?

This is the question what was in my mind, and the data can provide the answer, as always.
So this was the goal of my project, to gather and analyze data about our national football team.
## The Pipeline

  - Available and callable open API
  - Java based program to build up json REST API calls
  - Java based program to push the gathered data into a database
  - MySQL database to write queries
  - BI tool to represent data
    
  ## About the API
I've called two parts of the structure:
- Fixture: Give back a JSONArray with basic information about the match (eg. scored goals, result in the first and second hald etc.)
- Statistics: Give back a JSONArray which contains detailed statistics about the match (eg. shots on goal, shots off goal, total shots etc.)
 
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

![alt text](https://github.com/zch93/footballAPI_pics/blob/main/example.png?raw=true)
