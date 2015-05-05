(function(){
    'use strict';
    var gulp = require('gulp');
    var less = require('gulp-less');
    var ngConstant = require('gulp-ng-constant');
    var util = require('gulp-util');
    var plugins = require('gulp-load-plugins')();
    var argv = require('yargs').argv;

    var paths = {
        appScripts: 'src/app/**/*.js'
    };

    gulp.task('scripts', function () {
        return gulp.src([paths.appScripts])
            .pipe(plugins.jshint())
            .pipe(plugins.jshint.reporter(require('jshint-stylish')))
            .pipe(plugins.size());
    });

    gulp.task('css', function(){
        return gulp.src(['src/styles/less/app.less'])
            .pipe(less())
            .pipe(gulp.dest('src/styles'));
    });

    gulp.task('watch', ['serve'], function () {
        var server = plugins.livereload();

        gulp.watch([
            'src/**/*.html',
            'src/app/**/*.js',
            'src/styles/*.css'
        ]).on('change', function (file) {
            console.log('File changed: ' + file.path);
            server.changed(file.path);
        });
        gulp.watch('src/styles/less/*.less', ['css']);
        gulp.watch(paths.appScripts, ['scripts']);
    });

    gulp.task('injectjs', function(){
        var target = gulp.src('./src/index.html');
        var sources = gulp.src([paths.appScripts]);

        return target.pipe(plugins.inject(sources, {relative: true}))
            .pipe(gulp.dest('./src'));

    });

    gulp.task('serve', ['connect'], function () {
        //require('opn')('http://localhost:9000');
    });

    gulp.task('connect', function () {
        var connect = require('connect');
        var app = connect()
            .use(require('connect-livereload')({ port: 35729 }))
            .use(connect.static('src'))
            .use(connect.directory('src'));

        require('http').createServer(app)
            .listen(9000)
            .on('listening', function () {
                console.log('Started connect web server on http://localhost:9000');
            });
    });

    gulp.task('constants', function () {
        var myConfig = require('./src/config.json');
        var env = argv.env || 'dev';
        var envConfig = myConfig[env];
        return ngConstant({
            constants: envConfig,
            name: 'app.config',
            wrap: "(function(){\n\t'use strict';\n\t<%= __ngModule %>})();",
            //wrap: 'commonjs',
            stream: true
        })
            .pipe(gulp.dest('./src/lib'));
    });
})();

