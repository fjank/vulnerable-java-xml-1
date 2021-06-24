# WARNING! Do not start this application on an internetfacing server!
## What is this?
A sample application demonstrating XXE Java vulnerabilities.

The application has several vulnerable endpoints, each for one of the different XML parsing
methods in java.

## Notes
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
Open localhost:8080 in a browser.
