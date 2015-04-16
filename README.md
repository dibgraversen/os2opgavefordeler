README for TopicRouter frontend
===
This project is the frontend project for OS2 Topic Router (Opgavefordeler). 

It is based on Angular and built with npm, Bower and Gulp.
 
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