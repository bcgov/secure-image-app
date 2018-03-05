//
// SecureImage
//
// Copyright © 2018 Province of British Columbia
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// Created by Jason Leach on 2018-01-10.
//

/* eslint-env es6 */

'use strict';

const gulp = require('gulp');
const babel = require('gulp-babel');
const clean = require('gulp-clean');
const apidoc = require('gulp-apidoc');

gulp.task('clean', () => gulp.src('build', { read: false })
  .pipe(clean({
    force: true,
  })));

gulp.task('transpile', ['clean'], () => gulp.src('server/**/*.js')
  .pipe(babel())
  .pipe(gulp.dest('build/server')));

gulp.task('copy-config', ['clean'], () => gulp.src('server/config/*.json')
  .pipe(gulp.dest('build/server/config')));

gulp.task('copy-node-config', ['clean'], () => gulp.src(['apidoc.json', 'package.json', 'package-lock.json'])
  .pipe(gulp.dest('build')));

gulp.task('copy-templates', ['clean'], () => gulp.src('templates/**')
  .pipe(gulp.dest('build/templates')));

gulp.task('copy-public', ['clean'], () => gulp.src('public/**')
  .pipe(gulp.dest('build/public')));

gulp.task('apidoc', ['clean', 'transpile', 'copy-node-config'], done => apidoc({
  src: 'build/',
  dest: 'build/public/doc/api',
  encoding: 'utf8',
  silent: true,
  includeFilters: ['server/.*\\.js$'],
}, done));

gulp.task('default', ['clean', 'transpile', 'copy-config',
  'copy-node-config', 'copy-templates', 'copy-public', 'apidoc',
]);
