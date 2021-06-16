# What is this?
A sample application demonstrating XXE Java vulnerabilities.
build and run the docker app with:
gradlew build
docker build --build-arg JAR_FILE=build/libs/\*.jar -t springio/gs-spring-boot-docker .
docker run -p 8080:8080 springio/gs-spring-boot-docker .

