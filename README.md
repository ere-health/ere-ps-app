# ere-ps-app
ERE Primary System Desktop Client Application for the Gematik TI


### Overview
The ere-ps-app comprises two main components. 

* The ere-ps-app back-end which is a Java 11 Quarkus (https://quarkus.io/) application.
* The ere-ps-app front-end UI, which is a browser based HTML, CSS and JavaScript application.

The repository for the front-end UI can be found [here](https://github.com/ere-health/front-end-ere.health)


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




### Running the ERE-PS-App Application

* #### Software Requirements
  1. Download and install the latest version of the OpenJDK 11 SDK. You can use your preferred 
     package manager software on your computer to handle this, or simply download an archive or 
     installer from a publishing site such as AdoptOpenJDK (https://adoptopenjdk.net/).  Make sure 
     to choose OpenJDK 11 (LTS) and the HotSpot version of the JVM.
     
  2. Download and install the latest version of Apache Maven (https://maven.apache.org/). 
    
  3. The latest Chrome Browser (https://www.google.com/chrome/).
 
    
* #### Running the Application (Development Mode)
  Open a terminal window and change to the parent ere-ps-app directory of the back-end and then run 
  the following commands:
  
  In order to make sure you are working with the latest front-end UI, run the following first:
  > git submodule update --init
  
  Then run:
  
  > mvn quarkus:dev
  
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
  You can access the front-end UI of the application by making reference to the following URL in a 
  Chrome browser only!
  
  > http://localhost:8080/frontend/app/src/index.html
  
  > ***Very Important! The front-end UI can only be accessed in the Chrome browser.***
  
#### Verify Correct Reference to the Latest Front-End UI Version

To check that the ere-ps-app back-end is correctly referencing the front-end 
UI and its latest version, run the following command at a terminal window from the ere-ps-app parent 
directory:

> git submodule status

You should see something similar to the following with the only difference being the preceding git
revision number:

>  7887a70e4dbe35cede4d286dc57bc3bba608a48d src/main/resources/META-INF/resources/frontend (heads/main)

### Environment Variables
#### List of Application Environment Variables
* **ERE_DIRECTORY_WATCHER_DIR** 

    Specifies the path of the watch-folder for new muster 16 PDFs. Paths can be absolute or 
    relative to the path location of the ere-ps-app.jar executable.  For Windows environments, make 
    sure to use the double backslash characters to represent Windows specific file separators 
    (i.e. \\).
  
  
* **ERE_CONNECTOR_TLS_CERT_TRUST_STORE_FILE**
    
    Specifies the path of the Titus Connector TLS certificate trust store. Paths can be 
    absolute or relative to the path location of the ere-ps-app.jar executable. For Windows 
    environments, make sure to use the double backslash characters to represent Windows specific 
    file separators (i.e. \\).
  
  
* **ERE_CONNECTOR_TLS_CERT_TRUST_STORE_PWD**

    Password for the Titus TLS certificate trust store. For Windows environments, make sure to use
    double quotes around numeric values that are to be interpreted as a string. 
  
  
* **ERE_VALIDATOR_VALIDATE_SIGN_REQUEST_BUNDLES_ENABLED**

    Enables or disables  the validation of incoming sign request bundles.  Sign request bundles
    that fail validation checks will be prevented from propagating to the BE prescription processing
    workflow. Do not add double quotation marks 
    around the configuration values.
  

* **MUSTER16_TEMPLATE_CONFIGURATION**

    Allows for configuring which parsing profile the SVGExtractor module should use on startup of 
    the application.

In the development `dev` profile, all environment variables have default values, provided through the
`application.properties` file. Under other profiles, certain variable values are required to be explicitly provided
as an environment variable.
Namely, `ERE_CONNECTOR_TLS_CERT_TRUST_STORE_FILE` and `ERE_CONNECTOR_TLS_CERT_TRUST_STORE_PWD`.

The .env file should be located in the root project folder (ere-ps-app).

> Important! Configure the .env file to be ignored and not checked into the source code repository.

In regard to file and directory paths, configure the values for the environment variables in the
.env file to reference paths on your local computer.

An example of the layout of the contents in the .env file is shown below:

```
ERE_DIRECTORY_WATCHER_DIR=<YOUR_LOCAL_PATH>/watch-pdf
ERE_CONNECTOR_TLS_CERT_TRUST_STORE_FILE=<YOUR_LOCAL_PATH>/ere-ps-app/src/test/resources/certs/ps_erp_incentergy_01.p12
ERE_CONNECTOR_TLS_CERT_TRUST_STORE_PWD=<SECRET_VALUE_ON_YOUR_COMPUTER>
```
