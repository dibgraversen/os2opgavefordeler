README for TopicRouter frontend
===
This project is the frontend project for OS2 Topic Router (Opgavefordeler). 

It is based on Angular and built with npm, Bower and Gulp.


Dependencies and VCS
---
Npm and Bower dependencies have been checked into git for availability. Compiled stylesheets, though generated source, 
has been checked in as well so that there is no dependency to run a compile task. 

Npm
---
Npm defines basic Node dependencies.
 
Bower
---
Bower is used for managing dev dependencies. Run `bower` install to make sure you have all the required dependencies. 

A .bowerrc file specifying src/lib as location for bower files is required.

Gulp
---
Gulp is used to build this project.

- Run `gulp css` to compile app.less.
- Run `gulp watch` to 'host' a version of the site and have the site refreshed when saving changes.
- Run `gulp injectjs` to add js files from src/app/\*\* into src/index.html.
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

