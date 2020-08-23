FROM adoptopenjdk/openjdk11:latest

RUN addgroup thau && adduser --ingroup thau thau 
USER thau:thau

WORKDIR /home/thau

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} thau.jar

ENTRYPOINT ["java","-jar","./thau.jar"]