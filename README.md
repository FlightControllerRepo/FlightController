Readme for FlightController App

What this project does:
This a basic implementation of an app to interact with an IRIS+ helicopter. The app connects to a telemetry radio plugged into an Android device, and interacts using Mavlink messages. This application is a capable of controlling the copter using voice and limited manual controls, and holds the onboard drone information. Our application is also paired with our Arduino project, such that this app can receive the collision status messages sent from the on board Arduino board, and handle them accordingly.


How this project works:
This project consists of 4 major application layers within the source tree. The speech layer handle speech and passes that to our main flight controller logic. The flight controller logic layer is responsible for handling requests and creating the appropriate low level messages to send to the IRIS+. The low level messaging layer is responsible for providing wrapper around the mavlink message protocol to communicate with the IRIS. Finally, sitting on top of all this, is our UI, responsible for handling input from the user. 

Understanding the source:
The source is pretty straightforward, however there are a few things to keep in mind. This was really a prototype experiment, so a lot of the source was written using knowledge gained from the bowels of the internet. If continuing this project, 3dr has there own version of an API to communicate with the IRIS, or any pixhawk equipment. I would recommend starting with that, as that has a very active development. 
For our project, there are a couple of features that may need explaining. First, most actions are controlled by events that are fired throughout application. Any custom UI components are in the ui package. The java mavlink library were packaged in a jar file included, however there still exist our own wrappers around that library
