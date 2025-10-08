# ere-ps-app
ERE Primary System Desktop Client Application for the Gematik TI

### The ere-ps-app comprises two main components.

* The ere-ps-app back-end which is a Java 17 Quarkus (https://quarkus.io/) application.
* The ere-ps-app front-end UI, which is a browser based HTML, CSS and JavaScript application.

The repository for the front-end UI can be found [here](https://github.com/ere-health/front-end-ere.health)

[A playlist with 22 short videos: ere.health Integration Program](https://www.youtube.com/playlist?list=PL-xPLOh9MOSDGPV8HmlWMmkH-6VNhP9H5)

## Documentation
1. [Installation](#Installation)
2. [Configuration](#Configuration)
   1. [Configuring the ERE-PS-App Front-End UI](#Configuring-the-ERE-PS-App-Front-End-UI)
   2. [Configuring the ERE-PS-App Back-End](#Configuring-the-ERE-PS-App-Back-End)
3. [Running the Application](#Running-the-ERE-PS-App-Application)
4. [Running Tests](#Running-Tests)
5. [Code Coverage](#Code-Coverage)
6. [Using the Application](#Using-the-Application)
7. [Additional Information](#Additional-Information)

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
* .env file at the root of the project
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
  
  Make sure you created a `.env` file with your connector-specific configuration, see [CONFIGURATION.md](CONFIGURATION.md).

  Then run (with titus)):
  
  > mvn quarkus:dev

  With RU:

 > mvn -Dquarkus.profile=RU quarkus:dev

  [(Examples to start the application in different profiles and example CLI Parameters can be found in the CONFIGURATION.md file)](CONFIGURATION.md)
 

  At this point, the application should be running as highlighted below.
  
  ```shell
  Listening for transport dt_socket at address: 43411
  __  ____  __  _____   ___  __ ____  ______ 
   --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
   -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
  --\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
  2024-05-14 17:26:26,620 INFO  [ca.uhn.fhi.uti.VersionUtil] (Quarkus Main Thread) HAPI FHIR version 7.0.2 - Rev 95beaec894
  2024-05-14 17:26:26,623 INFO  [ca.uhn.fhi.con.FhirContext] (Quarkus Main Thread) Creating new FHIR context for FHIR version [R4]
  2024-05-14 17:26:26,647 INFO  [io.und.websockets] (Quarkus Main Thread) UT026003: Adding annotated server endpoint class health.ere.ps.websocket.Websocket for path /websocket
  2024-05-14 17:26:26,954 INFO  [io.qua.sch.run.SimpleScheduler] (Quarkus Main Thread) No scheduled business methods found - Simple scheduler will not be started
  2024-05-14 17:26:27,053 INFO  [hea.ere.ps.ser.idp.cli.IdpClient] (Quarkus Main Thread) Initializing using url: https://idp-ref.zentral.idp.splitdns.ti-dienste.de//.well-known/openid-configuration
  2024-05-14 17:26:28,270 INFO  [ca.uhn.fhi.con.FhirContext] (Quarkus Main Thread) Creating new FHIR context for FHIR version [R4]
  2024-05-14 17:26:29,012 INFO  [io.quarkus] (Quarkus Main Thread) ere-ps-app 1.0.0-SNAPSHOT on JVM (powered by Quarkus 3.9.4) started in 5.855s. Listening on: http://0.0.0.0:8080 and https://0.0.0.0:8443
  2024-05-14 17:26:29,013 INFO  [io.quarkus] (Quarkus Main Thread) Profile RU activated. Live Coding activated.
  2024-05-14 17:26:29,014 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, resteasy, resteasy-client, resteasy-jsonb, scheduler, servlet, smallrye-context-propagation, smallrye-openapi, swagger-ui, vertx, websockets, websockets-client]
  ```
  You can access the front-end UI of the application by making reference to the following URL
  
  > http://localhost:8080/frontend/app/src/index.html
  
  To use the swagger ui go to:

  http://localhost:8080/q/swagger-ui/
  
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

## Running Tests

The ere-ps-app includes a comprehensive test suite with unit tests for Java components.

### Running Unit Tests

To run the unit tests, open a terminal window and change to the ere-ps-app directory, then execute:

> ./mvnw test

This command will:
* Compile both main and test sources
* Execute all JUnit tests (excluding "titus" group by default)
* Generate surefire reports in `target/surefire-reports/`
* Tests use Quarkus test profile "dev" with system properties for development mode

---
[Back to top](#Table-of-Contents)

## Code Coverage

The project uses the `quarkus-jacoco` extension to generate code coverage reports during test execution.

### Generating Coverage Reports

To generate and view code coverage reports:

1. Run tests with coverage collection (automatically includes quarkus-jacoco):
   > ./mvnw test

2. View the HTML report by opening:
   > target/jacoco-report/index.html

### Coverage Metrics

The report provides detailed metrics including:
* Instruction coverage percentage
* Branch coverage
* Line coverage
* Method and class coverage

---
[Back to top](#Table-of-Contents)

## Using-the-Application

### For prescribing

#### REST

[Example with JavaScript](src/test/resources/javascript/create-e-prescription-with-runtime-config.js)

Use the swagger UI:
http://localhost:8080/q/swagger-ui/

![](img/Swagger-UI.png?raw=true)


##### ... with comfort signature

The comfort signature can be enabled by sending an [ActivateComfortSignature](src/test/resources/websocket-messages/ActivateComfortSignature-2-With-EHBA.json) message through the websocket. The [response](src/test/resources/websocket-messages/ActivateComfortSignature-2-Response.json) will contain a userId that has to be used afterwards in the userId field to enable the usage of the comfort signature.

Another way of enabling the comfort signature is posting to the workflow/comfortsignature/activate endpoint with passing the ehba card handle in the header "X-eHBAHandle". The endpoint will return the userId.

#### Websocket

[Right now details in the README.md file in the websocket message folder](src/test/resources/websocket-messages/README.md)

##### ... with comfort signature

If you want to use the comfort signature you have to pass the user id that you got from the ActivateComfortSignature message in the connector.user-id field e.g.:

```json
{
...
  "runtimeConfig": {
    ...
    "connector.user-id": "50aa5d2a-6f14-43bc-85ea-d03f9bd49441"
    ...
  }
...
}

```



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
