FROM quay.io/wildfly/wildfly:35.0.1.Final-jdk17

# Install MySQL JDBC driver
COPY docker/mysql/ /opt/jboss/wildfly/modules/system/layers/base/com/mysql/main/

# Configure WildFly datasource
COPY docker/mysql/configure-datasource.cli /tmp/configure-datasource.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/configure-datasource.cli

# Deploy application
COPY target/Easy2Drive.war /opt/jboss/wildfly/standalone/deployments/

EXPOSE 8080
