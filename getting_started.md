Getting Started
===============
This file explains the steps to get started with working on this project.

Development Backend
-------------------
The project builds with Java 8. Make sure to enable this first.

This project provides a Wildfly 9, and a bit of configuration, from a Docker based setup. Build the Docker image from the Dockerfile in project root. Instructions for build, run and configuration is provided there.
Make sure to run the docker exec... to enable God Mode login in dev.
 
Database setup is H2. Check the persistence.xml to enable create-drop and dialects for dev and make sure that test and prod deployments do NOT create drop!

Bootstrap data is configurable in BootstrappingDataProviderSingleton, which can be triggered from a dev env login page. This page also provides an easy God Mode login.
 
Development Frontend
--------------------
Frontend is AngularJS based and built with Gulp.

Make sure you install dependencies with both NPM and bower.

When all set up, run

`gulp watch-dev` 

to get a dev server running on port 9001 localhost.