#
# Wildfly setup class
#
class wildfly::setup {

  create_resources(wildfly::config::add_mgmt_user, $wildfly::users_mgmt)

  file { "${wildfly::dirname}/bin/${wildfly::mode}.conf":
    ensure  => file,
    owner   => $wildfly::user,
    group   => $wildfly::group,
    content => template('wildfly/standalone.conf.erb'),
    notify  => Class['wildfly::service']
  }

  if ($wildfly::mode == 'standalone') {

    wildfly::config::interfaces { 'public':
      inet_address_value => "\${jboss.bind.address:${wildfly::public_bind}}"
    }

    wildfly::config::interfaces { 'management':
      inet_address_value => "\${jboss.bind.address.management:${wildfly::mgmt_bind}}"
    }

    wildfly::config::socket_binding { 'management-http':
      port => "\${jboss.management.http.port:${wildfly::mgmt_http_port}}"
    }

    wildfly::config::socket_binding { 'management-https':
      port => "\${jboss.management.https.port:${wildfly::mgmt_https_port}}"
    }

    wildfly::config::socket_binding { 'http':
      port => "\${jboss.http.port:${wildfly::public_http_port}}"
    }

    wildfly::config::socket_binding { 'https':
      port => "\${jboss.https.port:${wildfly::public_https_port}}"
    }

    wildfly::config::socket_binding { 'ajp':
      port => "\${jboss.ajp.port:${wildfly::ajp_port}}"
    }
  }

}
