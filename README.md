# ere-ps-app
ERE Primary System Desktop Client Application for the Gematik TI

### The ere-ps-app comprises two main components.

* The ere-ps-app back-end which is a Java 11 Quarkus (https://quarkus.io/) application.
* The ere-ps-app front-end UI, which is a browser based HTML, CSS and JavaScript application.

The repository for the front-end UI can be found [here](https://github.com/ere-health/front-end-ere.health)

[A playlist with 22 short videos: ere.health Integration Program](https://www.youtube.com/playlist?list=PL-xPLOh9MOSDGPV8HmlWMmkH-6VNhP9H5)

## Documentation
1. [Installation](#Installation)
2. [Configuration](#Configuration)
   1. [Configuring the ERE-PS-App Front-End UI](#Configuring-the-ERE-PS-App-Front-End-UI)
   2. [Configuring the ERE-PS-App Back-End](#Configuring-the-ERE-PS-App-Back-End)
3. [Running the Application](#Running-the-ERE-PS-App-Application)
4. [Using the Application](#Using-the-Application)
5. [Additional Information](#Additional-Information)

---

## Installation

[Find details to installation on Windows and Linux systems in the INSTALL.md file](INSTALL.md)

---

## Configuration

### Configuring the ERE-PS-App Front-End UI

Open a terminal window and do the following:

* Clone the ere-ps-app back-end repository by running: 
  > git clone https://github.com/ere-health/ere-ps-app.git

* The repository of the front-end is setup as a submodule in the ere-ps-app repository. After 
  cloning the ere-ps-app repository or to reference the latest version of the frontend repository,
  run the following command:
  > git submodule update --init

* Cool tip! You can use one command which combines both of the previous commands to clone 
      the ere-ps-app repository, and to update the front-end UI submodule as shown below:
  
    > git clone --recurse-submodules https://github.com/ere-health/ere-ps-app.git 

The source files of the front-end UI will be located in the following directory location:
  > src/main/resources/META-INF/resources/frontend

At this point, you should now have access to the source files for both the backend and front-end of 
the application.

#### Updating the Front-End UI Submodule to Reference a Particular Branch in the Front-End UI Repo

Periodically, you may wish to update the front-end UI submodule to reference different branches 
in the front-end UI repository.  You can do this by entering the following commands as shown 
below from the ere-ps-app directory:

* Linux/MacOS
  1. >git submodule deinit --all
  2. >rm -rf .git/modules
  3. >git rm -rf src/main/resources/META-INF/resources/frontend
  4. >rm -rf src/main/resources/META-INF/resources/frontend
  5. >git submodule add -b < branch name > https://github.com/ere-health/front-end-ere.health.git src/main/resources/META-INF/resources/frontend

### Configuring the ERE-PS-App Back-End

#### Providing parameters

The ere-health application read configuration is the following order from most important to least important:
* RuntimeConfiguration as part of a web socket message or a HTTP header
* user.properties
* Java process parameter e.g. -Dquarkus.http.port=8081 on startup
* Environment variables e.g. export ERE_DIRECTORY_WATCHER_DIR="my-watch-dir"
* application.properties outside of the jar
* application.properties in the java class path (inside the jar)

[Find details to environment variables in the CONFIGURATION.md file](CONFIGURATION.md)

---

[Back to top](#Table-of-Contents)

### Running the ERE-PS-App Application

* #### Software Requirements
  1. Download and install the latest version of the OpenJDK 17 SDK. You can use your preferred 
     package manager software on your computer to handle this, or simply download an archive or 
     installer from a publishing site such as AdoptOpenJDK (https://adoptopenjdk.net/).  Make sure 
     to choose OpenJDK 17 (LTS) and the HotSpot version of the JVM.
     
  2. Download and install the latest version of Apache Maven (https://maven.apache.org/). 
    
  3. The latest Chrome, Firefox or Edge Browser
  (https://www.google.com/intl/en/chrome,
  https://www.mozilla.org/en-US/firefox/new,
  https://www.microsoft.com/en-us/edge).
 
    
* #### Running the Application (Development Mode)
  Open a terminal window and change to the parent ere-ps-app directory of the back-end and then run 
  the following commands:
  
  In order to make sure you are working with the latest front-end UI, run the following first:
  > git submodule update --init
  
  Then run:
  
  > mvn quarkus:dev

  [(Examples to start the application in different profiles and example CLI Parameters can be found in the CONFIGURATION.md file)](CONFIGURATION.md)
 

  At this point, the application should be running as highlighted below.
  
  ```shell
      __  ____  __  _____   ___  __ ____  ______ 
      --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
      -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
      --\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
      2021-06-08 15:45:41,324 INFO  [ca.uhn.fhi.uti.VersionUtil] (Quarkus Main Thread) HAPI FHIR version 5.3.0 - Rev 919c1dbddc
      2021-06-08 15:45:41,325 INFO  [ca.uhn.fhi.con.FhirContext] (Quarkus Main Thread) Creating new FHIR context for FHIR version [R4]
      2021-06-08 15:45:41,571 INFO  [io.und.websockets] (Quarkus Main Thread) UT026003: Adding annotated server endpoint class health.ere.ps.websocket.Websocket for path /websocket
      2021-06-08 15:45:41,665 INFO  [hea.ere.ps.ser.fs.DirectoryWatcher] (Quarkus Main Thread) Watching directory: /Users/douglas/my-indie-projects-work-area/ere-ps-app/watch-pdf
      2021-06-08 15:45:41,758 INFO  [io.quarkus] (Quarkus Main Thread) ere-ps-app 1.0.0-SNAPSHOT on JVM (powered by Quarkus 1.13.1.Final) started in 2.158s. Listening on: http://0.0.0.0:8080
      2021-06-08 15:45:41,760 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
      2021-06-08 15:45:41,761 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, rest-client, resteasy, resteasy-jsonb, scheduler, servlet, websockets]
  ```
  You can access the front-end UI of the application by making reference to the following URL
  
  > http://localhost:8080/frontend/app/src/index.html
  

  
#### Verify Correct Reference to the Latest Front-End UI Version

To check that the ere-ps-app back-end is correctly referencing the front-end 
UI and its latest version, run the following command at a terminal window from the ere-ps-app parent 
directory:

> git submodule status

You should see something similar to the following with the only difference being the preceding git
revision number:

>  7887a70e4dbe35cede4d286dc57bc3bba608a48d src/main/resources/META-INF/resources/frontend (heads/main)

---
[Back to top](#Table-of-Contents)

## Using-the-Application

### For prescribing

#### REST


##### ... with comfort signature



#### Websocket

[Right now details in the README.md file in the websocket message folder](src/test/resources/websocket-messages/README.md)

##### ... with comfort signature

#### Frontend 

##### ... with comfort signature


### For pharmacies
#### with CardLink

---
[Back to top](#Table-of-Contents)

## Additional Information

For general information and understanding the eRezept / prescribing workflow the [gematik E-Rezept API-Dokumentation](https://github.com/gematik/api-erp)
is a valuable source.


---
[Back to top](#Documentation)
