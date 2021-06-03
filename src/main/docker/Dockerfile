FROM azul/zulu-openjdk:11
EXPOSE 8080 7070

COPY launch.sh /service/launch.sh
RUN chmod 755 /service/launch.sh
COPY @project.build.finalName@.jar /service/app.jar
COPY jmx_prometheus_javaagent-0.15.0.jar /service/
RUN chmod 755 /service/jmx_prometheus_javaagent-0.15.0.jar
RUN chmod 755 /service/app.jar

ENTRYPOINT ["/service/launch.sh"]