var gulp = require('gulp');
var plugins = require('gulp-load-plugins')();
var del = require('del');
var es = require('event-stream');
var bowerFiles = require('main-bower-files');
var print = require('gulp-print');
var Q = require('q');

// == PATH STRINGS ========

var paths = {
	scripts: 'app/**/*.js',
	styles: './styles/*.css',
	images: './assets/*',
	index: './app/index.html',
	partials: ['app/**/*.html', '!app/index.html'],
	distDev: './dist.dev',
	distTest: './dist.test',
	distProd: './dist.prod',
	distScriptsProd: './dist.prod/scripts',
	scriptsDevServer: 'devServer/**/*.js'
};

// == PIPE SEGMENTS ========

var pipes = {};

pipes.orderedVendorScripts = function () {
	return plugins.order(['jquery.js', 'angular.js']);
};

pipes.orderedAppScripts = function () {
	return plugins.angularFilesort();
};

pipes.minifiedFileName = function () {
	return plugins.rename(function (path) {
		path.extname = '.min' + path.extname;
	});
};

pipes.validatedAppScripts = function () {
	return gulp.src(paths.scripts)
			.pipe(plugins.jshint())
			.pipe(plugins.jshint.reporter('jshint-stylish'));
};

pipes.builtAppScriptsDev = function () {
	return pipes.validatedAppScripts()
			.pipe(gulp.dest(paths.distDev + '/app'));
};

pipes.builtAppScriptsTest = function () {
	return pipes.validatedAppScripts()
			.pipe(gulp.dest(paths.distTest + '/app'))
};

pipes.builtAppScriptsProd = function () {
	var scriptedPartials = pipes.scriptedPartials();
	var validatedAppScripts = pipes.validatedAppScripts();

	return es.merge(scriptedPartials, validatedAppScripts)
			.pipe(pipes.orderedAppScripts())
			.pipe(plugins.sourcemaps.init())
			.pipe(plugins.concat('app.min.js'))
			.pipe(plugins.uglify())
			.pipe(plugins.sourcemaps.write())
			.pipe(gulp.dest(paths.distScriptsProd));
};

pipes.builtVendorScriptsDev = function () {
	return gulp.src(bowerFiles(), {base: './lib'})
			.pipe(gulp.dest(paths.distDev + '/lib'));
};

pipes.builtVendorScriptsTest = function () {
	return gulp.src(bowerFiles(), {base: './lib'})
			.pipe(gulp.dest(paths.distTest + '/lib'));
};

pipes.builtVendorScriptsProd = function () {
	return gulp.src(bowerFiles('**/*.js'))
			.pipe(pipes.orderedVendorScripts())
			.pipe(plugins.concat('vendor.min.js'))
			.pipe(plugins.uglify())
			.pipe(gulp.dest(paths.distScriptsProd));
};

pipes.validatedDevServerScripts = function () {
	return gulp.src(paths.scriptsDevServer)
			.pipe(plugins.jshint())
			.pipe(plugins.jshint.reporter('jshint-stylish'));
};

pipes.validatedPartials = function () {
	return gulp.src(paths.partials)
			.pipe(plugins.htmlhint({'doctype-first': false}))
			.pipe(plugins.htmlhint.reporter());
};

pipes.builtPartialsDev = function () {
	return pipes.validatedPartials()
			.pipe(gulp.dest(paths.distDev + '/app'));
};

pipes.builtPartialsTest = function () {
	return pipes.validatedPartials()
			.pipe(gulp.dest(paths.distTest + '/app'));
};

pipes.scriptedPartials = function () {
	return pipes.validatedPartials()
			.pipe(plugins.htmlhint.failReporter())
			.pipe(plugins.htmlmin({collapseWhitespace: true, removeComments: true}))
			.pipe(plugins.ngHtml2js({
				moduleName: "topicRouter"
			}));
};

pipes.builtStylesDev = function () {
	return gulp.src(paths.styles)
			.pipe(plugins.less())
			.pipe(gulp.dest(paths.distDev + '/styles'));
};

pipes.builtStylesTest = function () {
	return gulp.src(paths.styles)
			.pipe(plugins.less())
			.pipe(gulp.dest(paths.distTest + '/styles'));
};

pipes.builtStylesProd = function () {
	return gulp.src(paths.styles)
			.pipe(plugins.sourcemaps.init())
			.pipe(plugins.less())
			.pipe(plugins.minifyCss())
			.pipe(plugins.sourcemaps.write())
			.pipe(pipes.minifiedFileName())
			.pipe(gulp.dest(paths.distProd + '/styles'));
};

pipes.processedImagesDev = function () {
	return gulp.src(paths.images)
			.pipe(gulp.dest(paths.distDev + '/assets/'));
};

pipes.processedImagesTest = function () {
	return gulp.src(paths.images)
			.pipe(gulp.dest(paths.distTest + '/assets/'));
};

pipes.processedImagesProd = function () {
	return gulp.src(paths.images)
			.pipe(gulp.dest(paths.distProd + '/assets/'));
};

pipes.validatedIndex = function () {
	return gulp.src(paths.index)
			.pipe(plugins.htmlhint())
			.pipe(plugins.htmlhint.reporter());
};

pipes.config = function (env, target) {
	var myConfig = require('./config/config.json');
	var envConfig = myConfig[env];
	return plugins.ngConstant({
		constants: envConfig,
		name: 'app.config',
		wrap: "(function(){\n\t'use strict';\n\t<%= __ngModule %>})();",
		stream: true
	}).pipe(gulp.dest(target + '/app'));
};

pipes.builtIndexDev = function () {

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

pipes.builtIndexTest = function () {

	var orderedVendorScripts = pipes.builtVendorScriptsTest()
			.pipe(pipes.orderedVendorScripts());

	var orderedAppScripts = pipes.builtAppScriptsTest()
			.pipe(pipes.orderedAppScripts());

	var appStyles = pipes.builtStylesTest();

	return pipes.validatedIndex()
			.pipe(gulp.dest(paths.distTest)) // write first to get relative path for inject
			.pipe(plugins.inject(orderedVendorScripts, {relative: true, name: 'bower'}))
			.pipe(plugins.inject(orderedAppScripts, {relative: true}))
			.pipe(plugins.inject(appStyles, {relative: true}))
			.pipe(gulp.dest(paths.distTest));
};

pipes.builtIndexProd = function () {

	var vendorScripts = pipes.builtVendorScriptsProd();
	var appScripts = pipes.builtAppScriptsProd();
	var appStyles = pipes.builtStylesProd();

	return pipes.validatedIndex()
			.pipe(gulp.dest(paths.distProd)) // write first to get relative path for inject
			.pipe(plugins.inject(vendorScripts, {relative: true, name: 'bower'}))
			.pipe(plugins.inject(appScripts, {relative: true}))
			.pipe(plugins.inject(appStyles, {relative: true}))
			.pipe(plugins.htmlmin({collapseWhitespace: true, removeComments: true}))
			.pipe(gulp.dest(paths.distProd));
};

pipes.builtAppDev = function () {
	return es.merge(pipes.builtIndexDev(), pipes.config('dev', paths.distDev), pipes.builtPartialsDev(), pipes.processedImagesDev());
};

pipes.builtAppTest = function () {
	return es.merge(pipes.builtIndexTest(), pipes.config('test', paths.distTest), pipes.builtPartialsTest(), pipes.processedImagesTest());
};

// TODO verify
pipes.builtAppProd = function () {
	return es.merge(pipes.builtIndexProd(), pipes.processedImagesProd());
};

// == TASKS ========

// removes all compiled dev files
gulp.task('clean-dev', function () {
	var deferred = Q.defer();
	del(paths.distDev, function () {
		deferred.resolve();
	});
	return deferred.promise;
});

// removes all compiled test files
gulp.task('clean-test', function () {
	var deferred = Q.defer();
	del(paths.distTest, function () {
		deferred.resolve();
	});
	return deferred.promise;
});

// removes all compiled production files
gulp.task('clean-prod', function () {
	var deferred = Q.defer();
	del(paths.distProd, function () {
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

// moves html source files into the test environment
gulp.task('build-partials-test', pipes.builtPartialsTest);

// converts partials to javascript using html2js
gulp.task('convert-partials-to-js', pipes.scriptedPartials);

// runs jshint on the dev server scripts
gulp.task('validate-devserver-scripts', pipes.validatedDevServerScripts);

// runs jshint on the app scripts
gulp.task('validate-app-scripts', pipes.validatedAppScripts);

// moves app scripts into the dev environment
gulp.task('build-app-scripts-dev', pipes.builtAppScriptsDev);

// moves app scripts into the test environment
gulp.task('build-app-scripts-test', pipes.builtAppScriptsTest);

// concatenates, uglifies, and moves app scripts and partials into the prod environment
gulp.task('build-app-scripts-prod', pipes.builtAppScriptsProd);

// compiles app sass and moves to the dev environment
gulp.task('build-styles-dev', pipes.builtStylesDev);

// compiles app sass and moves to the test environment
gulp.task('build-styles-test', pipes.builtStylesTest);

// compiles and minifies app sass to css and moves to the prod environment
gulp.task('build-styles-prod', pipes.builtStylesProd);

// moves vendor scripts into the dev environment
gulp.task('build-vendor-scripts-dev', pipes.builtVendorScriptsDev);

// moves vendor scripts into the test environment
gulp.task('build-vendor-scripts-test', pipes.builtVendorScriptsTest);

// concatenates, uglifies, and moves vendor scripts into the prod environment
gulp.task('build-vendor-scripts-prod', pipes.builtVendorScriptsProd);

// validates and injects sources into index.html and moves it to the dev environment
gulp.task('build-index-dev', pipes.builtIndexDev);

// validates and injects sources into index.html and moves it to the test environment
gulp.task('build-index-test', pipes.builtIndexTest);

// validates and injects sources into index.html, minifies and moves it to the dev environment
gulp.task('build-index-prod', pipes.builtIndexProd);

// builds a complete dev environment
gulp.task('build-app-dev', pipes.builtAppDev);

// builds a complete dev environment
gulp.task('build-app-test', pipes.builtAppTest);

// builds a complete prod environment
gulp.task('build-app-prod', pipes.builtAppProd);

// cleans and builds a complete dev environment
gulp.task('clean-build-app-dev', ['clean-dev'], pipes.builtAppDev);

// cleans and builds a complete test environment
gulp.task('clean-build-app-test', ['clean-test'], pipes.builtAppTest);

// cleans and builds a complete prod environment
gulp.task('clean-build-app-prod', ['clean-prod'], pipes.builtAppProd);

// clean, build, and watch live changes to the dev environment
gulp.task('watch-dev', ['clean-build-app-dev', 'validate-devserver-scripts'], function () {

	// start nodemon to auto-reload the dev server
	plugins.nodemon({script: 'server.js', ext: 'js', watch: ['devServer/'], env: {NODE_ENV: 'development'}})
			.on('change', ['validate-devserver-scripts'])
			.on('restart', function () {
				console.log('[nodemon] restarted dev server');
			});

	// start live-reload server
	plugins.livereload.listen({start: true});

	// watch index
	gulp.watch(paths.index, function () {
		return pipes.builtIndexDev()
				.pipe(plugins.livereload());
	});

	// watch app scripts
	gulp.watch(paths.scripts, function () {
		return pipes.builtAppScriptsDev()
				.pipe(plugins.livereload());
	});

	// watch html partials
	gulp.watch(paths.partials, function () {
		return pipes.builtPartialsDev()
				.pipe(plugins.livereload());
	});

	// watch styles
	gulp.watch(paths.styles, function () {
		return pipes.builtStylesDev()
				.pipe(plugins.livereload());
	});

});

// clean, build, and watch live changes to the dev environment
// TODO won't work 'cause of settings file.
gulp.task('watch-test', ['clean-build-app-test', 'validate-devserver-scripts'], function () {

	// start nodemon to auto-reload the dev server
	plugins.nodemon({script: 'server.js', ext: 'js', watch: ['devServer/'], env: { NODE_ENV: 'test' }})
			.on('change', ['validate-devserver-scripts'])
			.on('restart', function () {
				console.log('[nodemon] restarted test server');
			});

	// start live-reload server
	plugins.livereload.listen({start: true});

	// watch index
	gulp.watch(paths.index, function () {
		return pipes.builtIndexTest()
				.pipe(plugins.livereload());
	});

	// watch app scripts
	gulp.watch(paths.scripts, function () {
		return pipes.builtAppScriptsTest()
				.pipe(plugins.livereload());
	});

	// watch html partials
	gulp.watch(paths.partials, function () {
		return pipes.builtPartialsTest()
				.pipe(plugins.livereload());
	});

	// watch styles
	gulp.watch(paths.styles, function () {
		return pipes.builtStylesTest()
				.pipe(plugins.livereload());
	});

});

// clean, build, and watch live changes to the prod environment
gulp.task('watch-prod', ['clean-build-app-prod', 'validate-devserver-scripts'], function () {

	// start nodemon to auto-reload the dev server
	plugins.nodemon({script: 'server.js', ext: 'js', watch: ['devServer/'], env: {NODE_ENV: 'production'}})
			.on('change', ['validate-devserver-scripts'])
			.on('restart', function () {
				console.log('[nodemon] restarted dev server');
			});

	// start live-reload server
	plugins.livereload.listen({start: true});

	// watch index
	gulp.watch(paths.index, function () {
		return pipes.builtIndexProd()
				.pipe(plugins.livereload());
	});

	// watch app scripts
	gulp.watch(paths.scripts, function () {
		return pipes.builtAppScriptsProd()
				.pipe(plugins.livereload());
	});

	// watch hhtml partials
	gulp.watch(paths.partials, function () {
		return pipes.builtAppScriptsProd()
				.pipe(plugins.livereload());
	});

	// watch styles
	gulp.watch(paths.styles, function () {
		return pipes.builtStylesProd()
				.pipe(plugins.livereload());
	});

});

// default task builds for prod
gulp.task('default', ['clean-build-app-prod']);