# ere-ps-app installation checklist

# Materials

- [ ]  URL of the connector in the local network
    - Can be found in the settings of the PVS
- [ ]  Card reader
- [ ]  SMC-B card
- [ ]  eHBA with PIN

# Installation

1. Download [Teamviewer](https://www.teamviewer.com/en/download/windows/) and log in with donges@am.gmbh
2. Install [git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
3. Install [Microsoft OpenJDK (Java 11)](https://docs.microsoft.com/en-gb/java/openjdk/download)
4. Set Java Home `setx -m JAVA_HOME "<path of JDK>"` 
5. Install [VS Code](https://code.visualstudio.com/download)
    1. Install Plugin for Java Tests
    2. If needed, configure Java version in VS Code settings (settings.json)
6. `git clone [https://github.com/ere-health/ere-ps-app.git](https://github.com/ere-health/ere-ps-app.git)`
    1. Install frontend with `git submodule update —init`
    2. Create .env file in `ere-ps-app/` with path to watch-folder, according to the [README.md](http://readme.md) configuration
7. Run test cases with `mvnw.cmd verify` 
8. Start ere-ps application with `mvnw.cmd quarkus:dev`

# Configuration of [application.properties](http://application.properties)

1. Set Fachdienst base url to
    1. Production: [`https://idp.zentral.idp.splitdns.ti-dienste.de`](https://idp.zentral.idp.splitdns.ti-dienste.de/)
    2. RU: [`https://idp-ref.zentral.idp.splitdns.ti-dienste.de/`](https://idp-ref.zentral.idp.splitdns.ti-dienste.de/) 
2. Set route of 
3. Set clientId to
    1. Production: `GEMIncenereS2QmFN83P`
    2. RU: `GEMIncenereSud1PErUR`
4. Replace base url for connector services with url of the connector in the local network
    1. Example: [`https://192.168.1.60/`](https://192.168.1.60/)
5. Set `enableVau` to `true`
6. Configure connector context. Data can be found in the connector / PVS settings
    - In `[ERezeptWorkflowServiceTest.java](http://erezeptworkflowservicetest.java)` run `testGetCards()` and fill in the returned cardHandle for the eHBA & SMCB into the cardHandle configuration
7. (Outstanding ToDO) Set the `connector-version` to respective version of the connector (PTV3, PTV4, PTV4+)