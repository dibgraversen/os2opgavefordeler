#!/usr/bin/env python

from fabric.api import *


def download(url, destination):
    run("wget '{}' -O {}".format(url, destination))


def jboss_cli(cmd):
    run("/opt/wildfly/bin/jboss-cli.sh --connect -u=mgmtuser -p=mgmtuser --command='%s'" % cmd)


def liquibase(cmd):
    run("liquibase --classpath='./TopicRouter.war' --url='jdbc:postgresql://localhost/topicrouter' --username='topicrouter' --password='SuperSaltFisk' --driver='org.postgresql.Driver' --changeLogFile='/WEB-INF/classes/db/migration/changelog-master.xml' %s" % cmd)


def deploy_webapp(version):
    """
    :param version: Version of webapp to deploy
    :return:
    """


def deploy_backend(version):
    """
    :param version: Version of TopicRouter to deploy
    :return:
    """
    if "snapshot" in version.lower():
        download(
            "http://nexus.miracle.local/nexus/service/local/artifact/maven/redirect?r=snapshots&g=dk.os2opgavefordeler&a=TopicRouter&v={}&e=war".format(
                version), "TopicRouter.war")
    else:
        download(
            "http://nexus.miracle.local/nexus/service/local/artifact/maven/redirect?r=releases&g=dk.os2opgavefordeler&a=TopicRouter&v={}&e=war".format(
                version), "TopicRouter.war")

    liquibase('clearCheckSums')
    liquibase('update')
    jboss_cli("deploy TopicRouter.war --force".format(version))


def deploy(version):
    deploy_backend(version)
    deploy_webapp(version)
