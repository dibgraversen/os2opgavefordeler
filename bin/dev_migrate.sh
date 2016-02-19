#!/usr/bin/env bash
fab -H localhost:2223 -u vagrant get_war:local liquibase:update