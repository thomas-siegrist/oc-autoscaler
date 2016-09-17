FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
EXPOSE 8092
ADD autoscale-stats-0.0.1-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]