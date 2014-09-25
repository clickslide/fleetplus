fleetplus
=========
Fleet management for Variable Inc. NODE+ sensors

Setup
=====
1. Download the sample app from https://bitbucket.org/variabletech/libnode-android-public/overview
2. Download these files
3. Replace the API Demo folder with this folder.
4. Add Ion, AndroidAsync, and Gson JARs to your project
5. publish the app to your Android device
6. Open the dashboard (link below)
7. Watch your data stream into the dashboard.

Dashboard
=========
http://variableinc.herokuapp.com/dashboard.html

Server Code
===========
The server is made with Node.js. It is a very simple async relay that routes all incoming messages back to the dashboard. We used an open source project ActionHero.js to build the server. You can download that project here:

http://actionherojs.com

Datadipity
==========
All of the API integration works via Datadipity. https://clickslide.co for more info.
Datadipity uses Clickslide's API automation technology Natural Machine Language to connect, authentication, and communicate with the following APIs:

Parse.com - This is use to store all the data in time series. During testing and development we created 4,000 records in a single Parse database.

ATT In App Messaging - This is used to send SMS from our app when the light levels drop below 1 LUX.

Twitter - This is used to create a message stream when the light levels drop below 1 LUX.

Realtime Server - This was created with actionhero. We send all data from the App through datadipity and then finally to the real time Node.js server. The server uses Faye to connect via websockets.


Additional Features
===================
When the light levels go below a LUX value of 1, I'm notified via SMS and the NODE+ sensor sends a Tweet for me!
https://twitter.com/ClickSlideCTO
