# Configuration

[Back to Readme](README.md)

## List of parameters

### Quarkus Dev UI

If you started the application with `mvn quarkus:dev` you can access the quarkus Dev UI under `http://localhost:8080/q/dev/`

- The Config Editor shows the current configuration of the application, including the environment variables, with a handy search function.
   - e.g. `connector.client-system-id` shows the settings in different profiles (if preceded with %'name-of-profile') and the current setting without it.
- ArC links allow to see the current state of the application and the beans in the application.

---

### Startup parameters

If you want to use a special profile ("%RU." prefix in files for "RU" profile) use:
> mvn -Dquarkus.profile=RU quarkus:dev

- If you want to see the SOAP message between ere-ps-app and the konnektor use:
> mvn -Djvm.args="-Dcom.sun.xml.ws.transport.http.client.HttpTransportPipe.dump=true -Dcom.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump=true -Dcom.sun.xml.ws.transport.http.HttpAdapter.dump=true -Dcom.sun.xml.internal.ws.transport.http.HttpAdapter.dump=true -Dcom.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold=999999" quarkus:dev

- If you want so see the SSL Handshake use:
> mvn -Djvm.args="-Djavax.net.debug=ssl:handshake" quarkus:dev

---

### Environment Variables

For Quarkus specific variables, please refer to the [Quarkus documentation](https://quarkus.io/guides/config-reference),
keep version in mind, as the documentation might change.

#### List of Application Environment Variables

* **ERE_DIRECTORY_WATCHER_DIR**

  > directory-watcher.dir=${ERE_DIRECTORY_WATCHER_DIR:watch-pdf}

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

####  Table

---

[Back to Readme](README.md)