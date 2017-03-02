# build this docker image with
# docker build -t opgf .

FROM jboss/wildfly:9.0.2.Final

# Add mgmt user
RUN /opt/jboss/wildfly/bin/add-user.sh admin admin --silent

# Add the jboss-cli commands for postgres
ADD environment/docker/commands.cli /tmp/system_properties.cli

ADD sources/TopicRouter/target/TopicRouter.war /opt/jboss/wildfly/standalone/deployments/

# Expose the ports we're interested in
EXPOSE 8080 9990

# Set the default entrypoint to run on boot
# This will boot WildFly in the standalone mode and bind to all interface
ENTRYPOINT ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]

# run with: -p is port mappings and -v sets up a folder share for doing deployments
# docker run -p 8080:8080 -p 9990:9990 -v /tmp/deployments:/opt/jboss/wildfly/standalone/deployments/:rw opgf
# to run cli script - if enabled here, it's run before the jboss is started and therefor fails.
# docker exec <docker ps id here> /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/system_properties.cli --connect
