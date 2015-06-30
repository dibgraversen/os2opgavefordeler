Welcome to TaskRouter for OS2
===
This part of the project is the REST based backend.

Documentation
---
A docs folder exists and contains a Software Guidebook as well as a domain diagram.


Requirements
---
Java 8 runtime.
JBoss EAP 6.4 - earlier versions don't have proper Java 8 support.
PostgreSQL 9.2.10.

Authentication: done with OpenID Connect, using the connect2id library:
https://bitbucket.org/connect2id/oauth-2.0-sdk-with-openid-connect-extensions

