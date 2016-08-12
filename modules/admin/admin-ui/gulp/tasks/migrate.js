/*
 Migrate from TS modules .
 */

var CONFIG = require("../config");
var gulp = require("gulp");
var gulpSequence = require("gulp-sequence");
var insert = require("gulp-insert");
var replace = require('gulp-replace');
var del = require("del");
var path = require("path");
var logger = require("../util/compileLogger");

function resolvePath(filePath) {
    return path.join(CONFIG.root.src, filePath);
}

function resolvePaths(filePaths) {
    filePaths = filePaths || [];
    return filePaths.map(resolvePath);
}

// Step 1
// remove _module.ts
gulp.task('migrate:1', function (cb) {
    var cleanPaths = resolvePath('/common/js/**/_module.ts');

    return del(cleanPaths)
        .catch(function (e) {
            logger.pipeError(cb, e);
        })
        .then(function (files) {
            logger.log("Cleaned " + (files && files.length || 0) + " file(s).");
        });
});

// Step 2
// Create a map for each export
gulp.task('migrate:2', function (cb) {
    var tsPaths = resolvePath('/common/js/**/*.ts');
    var basePath = resolvePath('/common/js/');

    return gulp.src(tsPaths, {base: basePath})
        .pipe(insert.transform(function (contents, file) {
            var module = findModule(contents)[0];
            if (module) {
                var exports = findExports(contents);
                exports.forEach(function (value) {
                    pathsMap.push({
                        name: value,
                        module: module,
                        full: module + '.' + value,
                        path: file.path
                    });
                });
            }
            return contents;
        }));
});

const pathsMap = [
    // name: Name
    // module: api.dom.Name
    // path: d:/../js/dom/Name.ts
];

function findAll(regex, content) {
    var match;
    var result = [];

    while ((match = regex.exec(content)) !== null) {
        result.push(match[1]);
    }

    return result;
}

function findExports(content) {
    var exportPattern = /(?:[\s\n]*export\s+(?:class|interface)\s+)([A-Z]{1}\w+)/g;
    return findAll(exportPattern, content);
}

function findModule(content) {
    var modulePattern = /(?:module\s+)([A-Za-z\.]+)(?:\s+\{\s*\n*)/g;
    return findAll(modulePattern, content);
}

// Step 3
// remove module declaration and add api.ts imports
gulp.task('migrate:3', function (cb) {
    var tsPaths = resolvePath('/common/js/**/*.ts');
    var basePath = resolvePath('/common/js/');

    var bracketPattern = /}[\s*\n]*$/g;
    var modulePattern = /module\s+[A-Za-z\.]*\s+\{\s*\n*/g;

    return gulp.src(tsPaths, {base: basePath})
        .pipe(replace(modulePattern, ''))
        .pipe(replace(bracketPattern, ''))
        .pipe(insert.transform(function (contents, file) {
            var relativePath = path.relative(path.dirname(file.path), basePath);
            relativePath = relativePath ? path.normalize(relativePath).replace(/\\/g, '/') : '.';
            var importApi = 'import "' + relativePath + '/api.ts";\n\n';
            return importApi + contents;
        }))
        .pipe(gulp.dest(basePath));
});

gulp.task('migrate', gulpSequence('migrate:1', 'migrate:2', 'migrate:3'));
