# Controller for accessing a graph database

## Usage
To launch it from IntelliJ, open AppGraphRepoApplication.java and click the Play button.

For now, this API accepts two requests:
- Send a GET request to /data?app_name to retrieve all the info saved for that app.
- Send a POST request to /insert with a json containing the app info you want to save.
  - Format the json as follows:
```
{
  "name": "OsmAnd",
  "description": "OsmAnd is an offline world map application based on OpenStreetMap (OSM), which allows you to navigate taking into account the preferred roads and vehicle dimensions. Plan routes based on inclines and record GPX tracks without an internet connection.\r\nOsmAnd is an open source app. We do not collect user data and you decide what data the app will have access to.\r\n\r\nMain features:\r\n\r\nMap view\r\n\u2022 Choice of places to be displayed on the map: attractions, food, health and more;\r\n\u2022 Search for places by address, name, coordinates, or category;\r\n\u2022 Map styles for the convenience of different activities: touring view, nautical map, winter and ski, topographic, desert, off-road, and others;\r\n\u2022 Shading relief and plug-in contour lines;\r\n\u2022 Ability to overlay different sources of maps on top of each other;\r\n\r\nGPS Navigation\r\n\u2022 Plotting a route to a place without an Internet connection;\r\n\u2022 Customizable navigation profiles for different vehicles: cars, motorcycles, bicycles, 4x4, pedestrians, boats, public transport, and more;\r\n\u2022 Change the constructed route, taking into account the exclusion of certain roads or road surfaces;\r\n\u2022 Customizable information widgets about the route: distance, speed, remaining travel time, distance to turn, and more;\r\n\r\nRoute Planning and Recording\r\n\u2022 Plotting a route point by point using one or multiple navigation profiles;\r\n\u2022 Route recording using GPX tracks;\r\n\u2022 Manage GPX tracks: displaying your own or imported GPX tracks on the map, navigating through them;\r\n\u2022 Visual data about the route - descents/ascents, distances;\r\n\u2022 Ability to share GPX track in OpenStreetMap;\r\n\r\nCreation of points with different functionality\r\n\u2022 Favourites;\r\n\u2022 Markers;\r\n\u2022 Audio/video notes;\r\n\r\nOpenStreetMap\r\n\u2022 Making edits to OSM;\r\n\u2022 Updating maps with a frequency of up to one hour;\r\n\r\nAdditional features\r\n\u2022 Android Auto support;\r\n\u2022 Compass and radius ruler;\r\n\u2022 Mapillary interface;\r\n\u2022 Night theme;\r\n\u2022 Wikipedia;\r\n\u2022 Large community of users around the world, documentation, and support;\r\n\r\nPaid features:\r\n\r\nMaps+ (in-app or subscription)\r\n\u2022 Unlimited map downloads;\r\n\u2022 Topo data (Contour lines and Terrain);\r\n\u2022 Nautical depths;\r\n\u2022 Offline Wikipedia;\r\n\u2022 Offline Wikivoyage - Travel guides;\r\n\r\nOsmAnd Pro (subscription)\r\n\u2022 All Maps+ features;\r\n\u2022 OsmAnd Cloud (backup and restore);\r\n\u2022 Pro features;\r\n\u2022 Hourly map updates.",
  "genre": "Travel & Local",
  "softwareVersion":"4.1.11",
  "releaseNotes":"\u2022 Added initial support for Android Auto\r\n\u2022 User interface update for UTM coordinate search\r\n\u2022 GPS Filter for GPX Tracks\r\n\u2022 Elevation Widget (Pro)\r\n\u2022 Favorites: added ability to view recently used icons\r\n\u2022 Route planning: will use the selected profile after launch\r\n\u2022 Fixed Mapillary layer, the plugin is now disabled by default\r\n\u2022 Added screen to manage all history in the app\r\n\u2022 Map orientation is not reset after restarting the app\r\n\u2022 Improved SRTM height marker rendering\r\n\u2022 Fixed Arabic map captions",
  "reviews": [
    {
      "review":"Great app. But reversing routes from a preset gpx file is harder than it should be. I did it once, now for the life of me, I can't do it again. Uninstalled to see I I'm missing something.",
      "reply":null
    },
    {
      "review":"Traffic Traffic jams and speed cams missing",
      "reply":null
    },
    {
      "review":"Good",
      "reply":null
    }
  ]
}      
```
      

