# Queries

Get all package applications
```SPARQL
SELECT ?subject ?predicate ?object
WHERE {
?subject ?predicate ?object
FILTER(STRSTARTS(STR(?subject), "https://schema.org/MobileApplication/"))
}
```

Get Application description
```
PREFIX schema: <https://schema.org/>
PREFIX mobileApplication: <https://schema.org/MobileApplication/>
SELECT ?text
WHERE {
  mobileApplication:air.com.aceviral.motox3m schema:description ?description.
  ?description schema:text ?text
}
```
Get Application name
```sparql
PREFIX schema: <https://schema.org/>
PREFIX mobileApplication: <https://schema.org/MobileApplication/>
SELECT ?name
WHERE {
  mobileApplication:air.com.aceviral.motox3m schema:name ?name.
}
```
Get All Application data
```sparql
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX schema: <https://schema.org/>
SELECT ?name ?description ?authorName ?reviewCount
WHERE {
    {
        SELECT ?name ?description ?authorName
        WHERE {
            ?app rdf:type schema:MobileApplication;
                 schema:name ?name ;
                 schema:abstract ?abstract ;
                 schema:author ?author .
            
            ?abstract schema:text ?description .
            ?author schema:author ?authorName .
            }
        LIMIT 20
    }
    {
        SELECT ?name (COUNT(?review) as ?reviewCount)
        WHERE {
            ?app rdf:type schema:MobileApplication;
                 schema:name ?name;
                 schema:review ?review .
        }
        GROUP BY ?name
    }
    FILTER (?name = ?name)
}
```

### SPARQL Query examples

Below we present a few query examples for the MApp-KG snapshot to illustrate its use in querying its data content. The folder ```data/sparql-examples``` contains one file for each of these examples with a built-in HTTP call using curl to access MApp-KG public instance.

You can run these examples by simply copying and pasting the content of any of these files in a terminal.

#### Example 1 - List all mobile apps

Get the name and the package of all apps available in MApp-KG.

###### SPARQL query

```PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX schema: <https://schema.org/>  SELECT ?app ?name ?package WHERE { ?app rdf:type schema:MobileApplication ; schema:name ?name ; schema:identifier ?package}```

###### Results (limited to 5 top results)

||app|name|package|
|-|-|-|-|
|1|https://schema.org/MobileApplication/org.iggymedia.periodtracker|"Flo\_Period_&_Pregnancy_Tracker"|"org.iggymedia.periodtracker"
|2|https://schema.org/MobileApplication/com.tapread.reader|"TapRead\_-\_Novels_&_Comics"|"com.tapread.reader"
|3|https://schema.org/MobileApplication/com.creditkarma.mobile|"Intuit_Credit_Karma"|"com.creditkarma.mobile"
|4|https://schema.org/MobileApplication/aplicacion.tiempo|"Weather\_Radar_-_Meteored_News"|"aplicacion.tiempo"
|5|https://schema.org/MobileApplication/com.free.hookup.dating.apps.wild|"Wild:_Hook_up,_Meet,_Dating_Me"|"com.free.hookup.dating.apps.wild"

#### Example 2 - List all mobile apps for a given category

Get the name and the package of all apps available in MApp-KG belonging to a given category (e.g., 'Tools')

###### SPARQL query

```PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX schema: <https://schema.org/> SELECT ?app ?name ?package WHERE {    ?app rdf:type schema:MobileApplication ; schema:applicationCategory "TOOLS" ; schema:name ?name ; schema:identifier ?package}```

###### Results (limited to 5 top results)

||app|name|package|
|-|-|-|-|
|1|https://schema.org/MobileApplication/com.duckduckgo.mobile.android|"DuckDuckGo_Private_Browser"|"com.duckduckgo.mobile.android"
|2|https://schema.org/MobileApplication/org.littlefreelibrary.littlefreelibrary|"Little_Free_Library"|"org.littlefreelibrary.littlefreelibrary"
|3|https://schema.org/MobileApplication/com.google.android.apps.translate|"Google_Translate"|"com.google.android.apps.translate"
|4|https://schema.org/MobileApplication/com.google.android.apps.accessibility.maui.actionblocks|"Action_Blocks"|"com.google.android.apps.accessibility.maui.actionblocks"
|5|https://schema.org/MobileApplication/com.google.android.apps.searchlite|"Google_Go"|"com.google.android.apps.searchlite"

#### Example 3 - Get all triplets for a given mobile app

###### SPARQL query

Get all triplets in MApp-KG for a given mobile app (e.g., Telegram: https://play.google.com/store/apps/details?id=org.telegram.messenger).

```PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX schema: <https://schema.org/> SELECT ?predicate ?object WHERE {    ?s schema:identifier "org.telegram.messenger" . ?s ?predicate ?object}```

###### Results (limited to 5 top results)

||predicate|object|
|-|-|-|
|1|rdf:type|https://schema.org/MobileApplication
|2|https://schema.org/identifier|"org.telegram.messenger"
|3|https://schema.org/author|https://schema.org/Organization/Telegram_FZ-LLC
|4|https://schema.org/datePublished|"2013-09-06T00:00:00.000+02:00"^^xsd:dateTime
|5|https://schema.org/dateModified|"2023-12-03T00:00:00.000+01:00"^^xsd:dateTime

#### Example 4 - Get all proprietary documents for a given mobile app

Get summary, description and changelogs (if available) for a given mobile app (e.g., Telegram: https://play.google.com/store/apps/details?id=org.telegram.messenger)

###### SPARQL query

```PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX schema: <https://schema.org/> SELECT * WHERE { { SELECT ?proprietaryDocument ?text WHERE {  ?s schema:identifier "com.mapswithme.maps.pro" ; schema:abstract ?proprietaryDocument .  ?proprietaryDocument schema:text ?text } } UNION  { SELECT ?proprietaryDocument ?text WHERE {   ?s schema:identifier "com.mapswithme.maps.pro" ; schema:description ?proprietaryDocument .  ?proprietaryDocument schema:text ?text  } } UNION { SELECT ?proprietaryDocument ?text WHERE {     ?s schema:identifier "com.mapswithme.maps.pro" ; schema:releaseNotes ?proprietaryDocument .  ?proprietaryDocument schema:text ?text }  } }```

###### Results

||proprietaryDocument|text|
|-|-|-|
|1|https://schema.org/DigitalDocument/org.telegram.messenger-SUMMARY|"Telegram is a messaging app with a focus on speed and security."
|2|https://schema.org/DigitalDocument/org.telegram.messenger-DESCRIPTION|"Pure instant messaging — simple, fast, secure, and synced across all your devices. One of the world's top 10 most downloaded apps with over 800 million active users. FAST: Telegram is the fastest messaging app on the market, connecting people via a unique, distributed network of data centers around the globe. SYNCED: You can access your messages from all your phones, tablets and computers at once. Telegram apps are standalone, so you don’t need to keep your phone connected. Start typing on one device and finish the message from another. Never lose your data again. UNLIMITED: You can send media and files, without any limits on their type and size. Your entire chat history will require no disk space on your device, and will be securely stored in the Telegram cloud for as long as you need it. SECURE: We made it our mission to provide the best security combined with ease of use. Everything on Telegram, including chats, groups, media, etc. is encrypted using a combination of 256-bit symmetric AES encryption, 2048-bit RSA encryption, and Diffie–Hellman secure key exchange. 100% FREE & OPEN: Telegram has a fully documented and free API for developers, open source apps and verifiable builds to prove the app you download is built from the exact same source code that is published. POWERFUL: You can create group chats with up to 200,000 members, share large videos, documents of any type (.DOCX, .MP3, .ZIP, etc.) up to 2 GB each, and even set up bots for specific tasks. Telegram is the perfect tool for hosting online communities and coordinating teamwork. RELIABLE: Built to deliver your messages using as little data as possible, Telegram is the most reliable messaging system ever made. It works even on the weakest mobile connections. FUN: Telegram has powerful photo and video editing tools, animated stickers and emoji, fully customizable themes to change the appearance of your app, and an open sticker/GIF platform to cater to all your expressive needs. SIMPLE: While providing an unprecedented array of features, we take great care to keep the interface clean. Telegram is so simple you already know how to use it. PRIVATE: We take your privacy seriously and will never give any third parties access to your data. You can delete any message you ever sent or received for both sides, at any time and without a trace. Telegram will never use your data to show you ads. For those interested in maximum privacy, Telegram offers Secret Chats. Secret Chat messages can be programmed to self-destruct automatically from both participating devices. This way you can send all types of disappearing content — messages, photos, videos, and even files. Secret Chats use End-to-End Encryption to ensure that a message can only be read by its intended recipient. We keep expanding the boundaries of what you can do with a messaging app. Don’t wait years for older messengers to catch up with Telegram — join the revolution today."
|3|https://schema.org/DigitalDocument/org.telegram.messenger-CHANGELOG|"* Upstream changelog at https://telegram.org/blog/reply-revolution"

#### Example 5 - Get all user reviews for a given mobile app

List all user reviews (author, review content and review score) for a given app (e.g., DuckDuckGo Browser: https://play.google.com/store/apps/details?id=com.duckduckgo.mobile.android)

###### SPARQL query

```PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX schema: <https://schema.org/> SELECT ?author ?reviewBody ?date ?reviewRating WHERE { ?s schema:identifier "com.duckduckgo.mobile.android" . ?s schema:review ?review . ?review schema:author ?author ; schema:reviewBody ?reviewBody ; schema:reviewRating ?reviewRating }  ORDER BY DESC(?date)```

###### Results (limited to 5 top results)

||author|reviewBody|publicationDate|reviewRating|
|-|-|-|-|
|1|"Saul Arellano"|"Very excellent for your everyday browsing."|"2023-12-05T00:00:00.000+01:00"^^xsd:dateTime|"5"^^xsd:int
|2|"vikesh kumar"|"No tracking privacy great"|"2023-12-05T00:00:00.000+01:00"^^xsd:dateTime|"5"^^xsd:int
|3|"Denni_isl"|"To much mainstream media"|"2023-12-05T00:00:00.000+01:00"^^xsd:dateTime|"3"^^xsd:int
|4|"Tyrone Mcintyre"|"DuckDuckGo is outstanding !! Just learning my system."|"2023-12-05T00:00:00.000+01:00"^^xsd:dateTime|"5"^^xsd:int
|5|"Nick Croz"|"quackers"|"2023-12-05T00:00:00.000+01:00"^^xsd:dateTime|"5"^^xsd:int

#### Example 6 - Get all annotated features for a given app

List all annotated features (i.e., features user-annotated in crowdsourced AlternativeTo platform) for a given app (e.g., WhatsApp Messenger: https://play.google.com/store/apps/details?id=com.whatsapp)

###### SPARQL query

```PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX schema: <https://schema.org/> SELECT ?feature WHERE { ?s schema:identifier "com.tomtom.gplay.navapp" . ?s schema:keywords ?feature }```

###### Results (limited to 5 top results)

||feature|
|-|-|
|1|https://schema.org/DefinedTerm/GPSNavigation
|2|https://schema.org/DefinedTerm/RealtimeTraffic
|3|https://schema.org/DefinedTerm/TurnbyturnNavigation
|4|https://schema.org/DefinedTerm/GPS
|5|https://schema.org/DefinedTerm/AppsWithOfflineMap

#### Example 7 - Get all apps containing a given annotated feature

List all apps annotated with a given annotated feature (e.g., TodoListManager)

###### SPARQL query

```PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX schema: <https://schema.org/> SELECT ?app WHERE { ?app schema:keywords <https://schema.org/DefinedTerm/TodoListManager> . }```

###### Results

||feature|
|-|-|
|1|https://schema.org/MobileApplication/com.google.android.apps.tasks
|2|https://schema.org/MobileApplication/com.qqlabs.minimalistlauncher

#### Example 8 - Get all features extracted from a given app

List all extracted features (i.e., features extracted using the Feature Extraction service) for a given app (e.g., WhatsApp Messenger: https://play.google.com/store/apps/details?id=com.whatsapp)

###### SPARQL query

```SELECT ?definedTerm WHERE { { <https://schema.org/MobileApplication/com.whatsapp> <https://schema.org/description> ?description . ?description <https://schema.org/keywords> ?definedTerm } UNION { <https://schema.org/MobileApplication/com.whatsapp> <https://schema.org/abstract> ?summary . ?summary <https://schema.org/keywords> ?definedTerm } UNION { <https://schema.org/MobileApplication/com.whatsapp> <https://schema.org/changelog> ?changelog . ?changelog <https://schema.org/keywords> ?definedTerm } }```

###### Results (limited to 5 top results)

||feature|
|-|-|
|1|https://schema.org/DefinedTerm/ContactProvider
|2|https://schema.org/DefinedTerm/HaveFeedback
|3|https://schema.org/DefinedTerm/ViewContat
|4|https://schema.org/DefinedTerm/UsePhoneInternetService
|5|https://schema.org/DefinedTerm/ShareMessage

#### Example 9 - Get all apps containing a given extracted feature

List all apps annotated with a given extracted feature (e.g., TodoListManager)

###### SPARQL query

```SELECT ?app WHERE { { ?app <https://schema.org/description> ?description . ?description <https://schema.org/keywords> <https://schema.org/DefinedTerm/SendMessage> } UNION { ?app <https://schema.org/abstract> ?summary . ?summary <https://schema.org/keywords> <https://schema.org/DefinedTerm/SendMessage> } UNION { ?app <https://schema.org/changelog> ?changelog . ?changelog <https://schema.org/keywords> <https://schema.org/DefinedTerm/SendMessage> } }```

###### Results (limited to 5 top results)

||feature|
|-|-|
|1|https://schema.org/MobileApplication/com.discord
|2|https://schema.org/MobileApplication/org.telegram.messenger
|3|https://schema.org/MobileApplication/jp.co.shueisha.mangaplus
|4|https://schema.org/MobileApplication/com.ourfamilywizard
|5|https://schema.org/MobileApplication/com.ldw.virtualfamilies2

#### Example 10 - Get all features semantically similar to a given feature

List all features that are semantically similar to a given feature (e.g., Send Message: https://schema.org/DefinedTerm/SendMessage).

##### SPARQL query

```SELECT ?feature WHERE { <https://schema.org/DefinedTerm/SendMessage> <https://schema.org/sameAs> ?feature}```

##### Results (limited to 5 top results)

||feature|
|-|-|
|1|https://schema.org/DefinedTerm/SendIntroductoryMessage
|2|https://schema.org/DefinedTerm/SendAsManyTextMessage
|3|https://schema.org/DefinedTerm/SendVoiceMessage
|4|https://schema.org/DefinedTerm/Message
|5|https://schema.org/DefinedTerm/SendFastMessage