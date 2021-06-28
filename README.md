# WARNING! Do not start this application on an internetfacing server!
## What is this?
A sample application demonstrating XXE Java vulnerabilities.

The application has several vulnerable endpoints, each for one of the different XML parsing
methods in java.

## Notes
* DOM: is served at the root (/) of the application, is vulnerable to XXE, but does not contain the actual attack. The other endpoints contains a default attack-vector.
* Schema: demonstrates error based XXE (only the beginning of the file is leaked).
* Validator: you need to start your own OOB server using FTP. Usually you also need to host
  an evil.dtd, but this server answers to evil.dtd, with defaults to requesting localhost:2121.
  For example https://github.com/staaldraad/xxeserv
  (only the beginning of the file is leaked, if you try to leak a file with newlines, an MalformedURLException will be thrown, and the OOB-XXE fails).
* TrAX: for xslt there is only the xml file to transform that is exposed in this app. But if you are in control of the stylesheet, XXE is fully possible there too.
  

build and run the docker app with:
```
gradlew build
docker build --build-arg JAR_FILE=build/libs/\*.jar -t fjank/vuln-xml .
docker run -p 8080:8080 fjank/vuln-xml .
```
Open localhost:8080 in a browser, and explore the various vulnerable-by-default parsers.

[the corresponding article for this repo](https://fjank.no/blog/2021-06-28-java-xml-security/).
