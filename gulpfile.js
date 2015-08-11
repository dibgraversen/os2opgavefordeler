(function () {
	'use strict';
	var gulp = require('gulp');
	var plugins = require('gulp-load-plugins')();
	var angularFileSort = require('gulp-angular-filesort');
	var argv = require('yargs').argv;
	var del = require('del');
	var es = require('event-stream');
	var bowerFiles = require('main-bower-files');
	var print = require('gulp-print');
	var Q = require('q');

	var paths = {
		scripts: './src/app/**/*.js',
		less: './src/styles/less/app.less',
		styles: './src/styles/*.css',
		images: './src/images/**/*',
		assets: './src/assets/**/*',
		index: './src/index.html',
		partials: ['./src/app/**/*.html', '!./src/index.html'],
		distDev: './dist.dev',
		distProd: './dist.prod',
		distScriptsProd: './dist.prod/scripts',
		scriptsDevServer: 'devServer/**/*.js'
	};

	// == PIPE SEGMENTS ========
	// This is inspired by 'A Healthy Gulp Setup for AngularJS Projects'.
	// https://github.com/paislee/healthy-gulp-angular/blob/master/gulpfile.js

	var pipes = {};

	pipes.orderedVendorScripts = function() {
		return plugins.order(['jquery.js', 'angular.js']);
	};

	pipes.orderedAppScripts = function() {
		return angularFileSort();
	};

	pipes.minifiedFileName = function() {
		return plugins.rename(function (path){
			path.extname = '.min' + path.extname;
		});
	};

	pipes.constants = function(){

	};

	pipes.validatedAppScripts = function() {
		return gulp.src(paths.scripts)
				.pipe(plugins.jshint())
				.pipe(plugins.jshint.reporter('jshint-stylish'));
	};

	pipes.builtAppScriptsDev = function() {
		return pipes.validatedAppScripts()
				.pipe(gulp.dest(paths.distDev));
	};

	pipes.builtVendorScriptsDev = function() {
		return gulp.src(bowerFiles(), { base: './src/lib' })
				.pipe(gulp.dest('dist.dev/lib'));
	};

	pipes.validatedDevServerScripts = function() {
		return gulp.src(paths.scriptsDevServer)
				.pipe(plugins.jshint())
				.pipe(plugins.jshint.reporter('jshint-stylish'));
	};

	pipes.validatedPartials = function() {
		return gulp.src(paths.partials)
				.pipe(plugins.htmlhint({'doctype-first': false}))
				.pipe(plugins.htmlhint.reporter());
	};

	pipes.builtPartialsDev = function() {
		return pipes.validatedPartials()
				.pipe(gulp.dest(paths.distDev + '/app'));
	};

	pipes.scriptedPartials = function() {
		return pipes.validatedPartials()
				.pipe(plugins.htmlhint.failReporter())
				.pipe(plugins.htmlmin({collapseWhitespace: true, removeComments: true}))
				.pipe(plugins.ngHtml2js({
					moduleName: "topicRouter"
				}));
	};

	pipes.builtStylesDev = function() {
		return gulp.src(paths.less)
				.pipe(plugins.less())
				.pipe(plugins.rename(function(path){
					path.dirname = '/styles';
				}))
				.pipe(gulp.dest(paths.distDev));
	};

	pipes.processedImagesDev = function() {
		return gulp.src(paths.images)
				.pipe(gulp.dest(paths.distDev + '/images/'));
	};

	pipes.assetsDev = function() {
		return gulp.src(paths.assets)
				.pipe(gulp.dest(paths.distDev + '/public/'));
	};

	pipes.validatedIndex = function() {
		return gulp.src(paths.index)
				.pipe(plugins.htmlhint())
				.pipe(plugins.htmlhint.reporter());
	};

	pipes.builtIndexDev = function() {

		var orderedVendorScripts = pipes.builtVendorScriptsDev()
				.pipe(pipes.orderedVendorScripts());

		var orderedAppScripts = pipes.builtAppScriptsDev()
				.pipe(pipes.orderedAppScripts());

		var appStyles = pipes.builtStylesDev();

		return pipes.validatedIndex()
				.pipe(gulp.dest(paths.distDev)) // write first to get relative path for inject
				.pipe(plugins.inject(orderedVendorScripts, {relative: true, name: 'bower'}))
				.pipe(plugins.inject(orderedAppScripts, {relative: true}))
				.pipe(plugins.inject(appStyles, {relative: true}))
				.pipe(gulp.dest(paths.distDev));
	};

	pipes.builtAppDev = function() {
		return es.merge(
				pipes.builtIndexDev(),
				pipes.builtPartialsDev(),
				pipes.processedImagesDev(),
				pipes.assetsDev());
	};

	// == TASKS =========

	// removes all compiled dev files
	gulp.task('clean-dev', function() {
		var deferred = Q.defer();
		del(paths.distDev, function() {
			deferred.resolve();
		});
		return deferred.promise;
	});

	// checks html source files for syntax errors
	gulp.task('validate-partials', pipes.validatedPartials);

// checks index.html for syntax errors
	gulp.task('validate-index', pipes.validatedIndex);

// moves html source files into the dev environment
	gulp.task('build-partials-dev', pipes.builtPartialsDev);

// converts partials to javascript using html2js
	gulp.task('convert-partials-to-js', pipes.scriptedPartials);

// runs jshint on the dev server scripts
	gulp.task('validate-devserver-scripts', pipes.validatedDevServerScripts);

// runs jshint on the app scripts
	gulp.task('validate-app-scripts', pipes.validatedAppScripts);

// moves app scripts into the dev environment
	gulp.task('build-app-scripts-dev', pipes.builtAppScriptsDev);

	// compiles app less and moves to the dev environment
	gulp.task('build-styles-dev', pipes.builtStylesDev);

	// moves vendor scripts into the dev environment
	gulp.task('build-vendor-scripts-dev', pipes.builtVendorScriptsDev);

	// validates and injects sources into index.html and moves it to the dev environment
	gulp.task('build-index-dev', pipes.builtIndexDev);

	// builds a complete dev environment
	gulp.task('build-app-dev', pipes.builtAppDev);

	// cleans and builds a complete dev environment
	gulp.task('clean-build-app-dev', ['clean-dev'], pipes.builtAppDev);

	// clean, build, and watch live changes to the dev environment
	gulp.task('watch-dev', ['clean-build-app-dev', 'validate-devserver-scripts'], function() {

		// start nodemon to auto-reload the dev server
		plugins.nodemon({ script: 'server.js', ext: 'js', watch: ['devServer/'], env: {NODE_ENV : 'development'} })
				.on('change', ['validate-devserver-scripts'])
				.on('restart', function () {
					console.log('[nodemon] restarted dev server');
				});

		// start live-reload server
		plugins.livereload.listen({ start: true });

		// watch index
		gulp.watch(paths.index, function() {
			return pipes.builtIndexDev()
					.pipe(plugins.livereload());
		});

		// watch app scripts
		gulp.watch(paths.scripts, function() {
			return pipes.builtAppScriptsDev()
					.pipe(plugins.livereload());
		});

		// watch html partials
		gulp.watch(paths.partials, function() {
			return pipes.builtPartialsDev()
					.pipe(plugins.livereload());
		});

		// watch styles
		gulp.watch(paths.styles, function() {
			return pipes.builtStylesDev()
					.pipe(plugins.livereload());
		});

	});

	// END 'A Healthy Gulp Setup for AngularJS Projects'.

	//gulp.task('scripts', function () {
	//	return gulp.src([paths.scripts])
	//			.pipe(plugins.jshint())
	//			.pipe(plugins.jshint.reporter(require('jshint-stylish')))
	//			.pipe(plugins.size());
	//});

	gulp.task('css', function () {
		return gulp.src([paths.less])
				.pipe(plugins.less())
				.pipe(gulp.dest('src/styles'));
	});

	//gulp.task('watch', ['serve'], function () {
	//	var server = plugins.livereload();
	//
	//	gulp.watch([
	//		'src/**/*.html',
	//		'src/app/**/*.js',
	//		'src/styles/*.css'
	//	]).on('change', function (file) {
	//		console.log('File changed: ' + file.path);
	//		server.changed(file.path);
	//	});
	//	gulp.watch('src/styles/less/*.less', ['css']);
	//	gulp.watch(paths.scripts, ['scripts']);
	//});

	gulp.task('injectjs', function () {
		var target = gulp.src('./src/index.html');
		var sources = gulp.src([paths.scripts])
				.pipe(angularFileSort());

		return target.pipe(plugins.inject(sources, {relative: true}))
				.pipe(gulp.dest('./src'));

	});

	gulp.task('serve', ['connect'], function () {
		//require('opn')('http://localhost:9000');
	});

	gulp.task('connect', function () {
		var connect = require('connect');
		var app = connect()
				.use(require('connect-livereload')({port: 35729}))
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
		return plugins.ngConstant({
			constants: envConfig,
			name: 'app.config',
			wrap: "(function(){\n\t'use strict';\n\t<%= __ngModule %>})();",
			//wrap: 'commonjs',
			stream: true
		})
				.pipe(gulp.dest('./src/app'));
	});
})();

