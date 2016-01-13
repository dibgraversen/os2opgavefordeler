node default {

  case $environment {
    'development': {
      $host = "http://localhost:1080"
    }
    'test': {
      $host = "http://topicrouter.miracle.dk"
    }
    default: {
      $host = "https://os2opgavefordeler.dk"
    }
  }

  include my_os
  include my_postgresql
  include my_apache
  include my_wildfly
}

class my_os {

  host{ 'localhost':
    ip           => "127.0.0.1",
    host_aliases => ['localhost.localdomain',
      'localhost4',
      'localhost4.localdomain4'],
  }

  service { iptables:
    enable    => false,
    ensure    => false,
    hasstatus => true,
  }


  class { 'selinux':
    mode => 'permissive'
  }

  include epel

  $install = ['binutils.x86_64','wget', 'java-1.8.0-openjdk-devel', 'liquibase']

  package { $install:
    ensure  => present,
  }
}

class my_apache {
  contain my_os

  class { 'apache':
    default_mods        => true,
    default_confd_files => true,
    default_vhost       => false
  }

  #apache::mod { 'proxy': }

  apache::vhost { 'localhost':
    vhost_name       => '*',
    port             => '80',
    docroot          => "/var/www/html",
    docroot_owner    => 'apache',
    docroot_group    => 'apache',
    proxy_pass       => [
      {
        'path'           => '/rest',
        'url'            => 'http://localhost:8080/TopicRouter/rest',
        'reverse_cookies'=>[
          { 'path' => '/TopicRouter',
            'url'  => '/'
          }
        ]
      },
    ],
  }
}

class my_postgresql {
  contain my_os

  class { 'postgresql::server':
    ip_mask_allow_all_users    => '0.0.0.0/0',
    listen_addresses           => '*',
    postgres_password          => 'password',
  }

  postgresql::server::db { 'topicrouter':
    user     => 'topicrouter',
    password => postgresql_password('topicrouter', 'SuperSaltFisk'),
  }

  postgresql::server::role { 'topicrouter_managers':
    password_hash => postgresql_password('topicrouter_managers', 'SuperSaltFisk'),
    superuser     => true,
  }

  postgresql::server::database_grant { 'test1':
    privilege => 'ALL',
    db        => 'topicrouter',
    role      => 'topicrouter_managers',
  }

  postgresql::validate_db_connection { 'validate my postgres connection':
    database_host           => 'localhost',
    database_username       => 'topicrouter',
    database_password       => 'SuperSaltFisk',
    database_name           => 'topicrouter',
    require                 => Postgresql::Server::Db['topicrouter']
  }
}


class my_wildfly{
  contain my_os, my_postgresql

  class { 'wildfly':
    version           => '9.0.2',
    install_source    => 'http://download.jboss.org/wildfly/9.0.2.Final/wildfly-9.0.2.Final.tar.gz',
    java_home         => '/usr/lib/jvm/java-1.8.0-openjdk',
    dirname           => '/opt/wildfly',
    users_mgmt        => { 'wildfly' => { username => 'wildfly', password => 'wildfly' } },
  }->

  wildfly::config::add_mgmt_user { 'Adding mgmtuser':
    username => 'mgmtuser',
    password => 'mgmtuser',
    require  => Class['wildfly'],
  }->

  wildfly::config::add_app_user { 'Adding appuser':
    username => 'appuser',
    password => 'appuser',
    require  => Class['wildfly'],
  }->

  wildfly::config::associate_groups_to_user { 'Associate groups to mgmtuser':
    username => 'mgmtuser',
    groups   => 'admin,mygroup',
    require  => Wildfly::Config::Add_mgmt_user['Adding mgmtuser'],
  }->

  wildfly::config::associate_roles_to_user { 'Associate roles to app user':
    username => 'appuser',
    roles    => 'guest,ejb',
    require  => Wildfly::Config::Add_app_user['Adding appuser'],
  }->

  wildfly::config::module { 'org.postgresql':
    source       => 'http://central.maven.org/maven2/org/postgresql/postgresql/9.3-1103-jdbc4/postgresql-9.3-1103-jdbc4.jar',
    dependencies => ['javax.api', 'javax.transaction.api'],
    require      => Class['wildfly'],
  } ->
  wildfly::datasources::driver { 'Driver postgresql':
    driver_name                     => 'postgresql',
    driver_module_name              => 'org.postgresql',
    driver_xa_datasource_class_name => 'org.postgresql.xa.PGXADataSource'
  } ->
  wildfly::datasources::datasource { 'OS2TopicRouterDS datasource':
    name           => 'OS2TopicRouterDS',
    config         => { 'driver-name'    => 'postgresql',
      'connection-url'                   => 'jdbc:postgresql://localhost/topicrouter',
      'jndi-name'                        => 'java:/OS2TopicRouterDS',
      'user-name'                        => 'topicrouter',
      'password'                         => 'SuperSaltFisk'
    }
  } ->

  notify{ 'host':
    message => "Host: $host"
  }->
  exec{ 'home add':
    command => '/opt/wildfly/bin/jboss-cli.sh --connect -u=mgmtuser -p=mgmtuser "/system-property=topicrouter.url.home:add()"',
    unless  => '/opt/wildfly/bin/jboss-cli.sh --connect -u=mgmtuser -p=mgmtuser "/system-property=topicrouter.url.home:read-resource()" | grep success'
  }->

  exec{ 'home set':
    command => "/opt/wildfly/bin/jboss-cli.sh --connect -u=mgmtuser -p=mgmtuser \"/system-property=topicrouter.url.home:write-attribute(name=value, value=\"$host\")\""
  }->

  exec{ 'openid add':
    command => '/opt/wildfly/bin/jboss-cli.sh --connect -u=mgmtuser -p=mgmtuser "/system-property=topicrouter.url.openid.callback:add()"',
    unless  => '/opt/wildfly/bin/jboss-cli.sh --connect -u=mgmtuser -p=mgmtuser "/system-property=topicrouter.url.openid.callback:read-resource" | grep success'
  }->

  exec{ 'openid set':
    command => "/opt/wildfly/bin/jboss-cli.sh --connect -u=mgmtuser -p=mgmtuser \"/system-property=topicrouter.url.openid.callback:write-attribute(name=value, value=\"$host/rest/auth/authenticate\")\""
  }->

  exec{ 'godmode add':
    command => '/opt/wildfly/bin/jboss-cli.sh --connect -u=mgmtuser -p=mgmtuser "/system-property=topicrouter.login.godmode.enabled:add()"',
    unless  => '/opt/wildfly/bin/jboss-cli.sh --connect -u=mgmtuser -p=mgmtuser "/system-property=topicrouter.login.godmode.enabled:read-resource" | grep success'
  }->

  exec{ 'godmode set':
    command => '/opt/wildfly/bin/jboss-cli.sh --connect -u=mgmtuser -p=mgmtuser "/system-property=topicrouter.login.godmode.enabled:write-attribute(name=value, value=true)"'
  }


}