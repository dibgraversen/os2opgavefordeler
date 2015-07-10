# TopicRouter server configuration

## Postgres config:
---

First, we configure general pgsql settings in **/var/lib/pgsql/data/postgresql.conf**:

    log_filename = 'postgresql-%Y-%m-%d.log'
    log_line_prefix = '%t %c %u@%h - '
    log_connections = on
    log_disconnections = on


Next, we need to configure the host-based authentication in **/var/lib/pgsql/data/pg_hba.conf**.

Our entry needs to be placed at the top - pgsql doesn't have fallthrough for auth,
so if an "entry that can service a request" doesn't authorize, the request is
blocked. Our entry is very specific, and thus won't block other connection attempts.

    host    topicrouter             topicrouter     127.0.0.1/32            md5

1. Column #1 specifices 'host' for TCP/IP based connections.
2. Column #2 is the allowed databases.
3. Column #3 is the role (user) that's allowed.
4. Column #4 restricts access to come from localhost.
5. Column #5 requires the client to send password as md5 hash.

After editing the configuration files, reload pgsql:

    sudo -u postgres pg_ctl reload -D /var/lib/pgsql/data/
or, with a kill-and-reload-demon:

    systemctl restart postgresql.service

Configure the database role/user (**change password!**):

    (bash) sudo -u postgres psql
    (psql) create role topicrouter with login encrypted password 'SuperSaltFisk';
    (psql) create database topicrouter with owner=topicrouter encoding='UTF8' lc_collate='da_DK.utf8' lc_ctype='da_DK.utf8';
    (psql) \q

Finally, test the setup - from a non-privileged user, perform:
	
    psql -U topicrouter -h 127.0.0.1 topicrouter



## Apache config:
---
Place supplied vhost-default.conf in **/etc/httpd/vhosts.d/default.conf**.

Make sure mod\_dir is enabled in **/etc/httpd/conf/httpd.conf**:

    LoadModule dir_module modules/mod_dir.so



## JBoss configuration
---
After installing JBOSS (and setting up JAVA\_HOME and JBOSS\_HOME), you need to set up an administrative user. Once that is done, we need to define our datasource - but first, we need to install the PostgreSQL adapter.

    (bash) sudo -u jboss curl -o /tmp/postgresql.jdbc41.jar https://jdbc.postgresql.org/download/postgresql-9.2-1004.jdbc41.jar
    (bash) sudo -u jboss /usr/share/jbossas/bin/jboss-cli.sh --connect
    (jboss-cli) module add --name=org.postgres --resources=/tmp/postgresql.jdbc41.jar --dependencies=javax.api,javax.transaction.api
    (jboss-cli) /subsystem=datasources/jdbc-driver=postgres:add(driver-name="postgres",driver-module-name="org.postgres",driver-class-name=org.postgresql.Driver)

After that, we add the datasource:

    (jboss-cli) data-source add --name=OS2TopicRouterDS --jndi-name=java:/OS2TopicRouterDS --enabled=true --driver-name=postgres --connection-url="jdbc:postgresql://localhost/topicrouter" --user-name=topicrouter --password=SuperSaltFisk
    (jboss-cli) /subsystem=datasources/data-source=OS2TopicRouterDS:enable()

And finally, clean up:

    (jboss-cli) quit
    (bash) rm /tmp/postgresql.jdbc41.jar


### JBoss hardening:
    /subsystem=deployment-scanner/scanner=default:write-attribute(name=scan-enabled, value=false)
    /subsystem=web/virtual-server=default-host:write-attribute(name="enable-welcome-root", value=false)
    /subsystem=web/configuration=jsp-configuration:write-attribute(name="x-powered-by", value=false)
    /subsystem=web/configuration=jsp-configuration:write-attribute(name="display-source-fragment", value=false)

