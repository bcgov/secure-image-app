//
// Code Signing
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

'use strict';

const gulp = require('gulp');
const babel = require('gulp-babel');
const clean = require('gulp-clean');

gulp.task('clean', () => gulp.src('build', { read: false, allowEmpty: true })
  .pipe(clean({
    force: true,
  })));

gulp.task('transpile-src', () => gulp.src(['src/**/*.js', '!src/**/__mocks__/**/*'])
  .pipe(babel())
  .pipe(gulp.dest('build/src')));

gulp.task('transpile-scripts', () => gulp.src('scripts/**/*.js')
  .pipe(babel())
  .pipe(gulp.dest('build/scripts')));

gulp.task('copy-config', () => gulp.src('src/config/*.json')
  .pipe(gulp.dest('build/src/config')));

gulp.task('copy-node-config', () => gulp.src(['package.json', 'package-lock.json'])
  .pipe(gulp.dest('build')));

gulp.task('copy-public', () => gulp.src('public/**')
  .pipe(gulp.dest('build/public')));

gulp.task('copy-templates', () => gulp.src('templates/**')
.pipe(gulp.dest('build/templates')));

gulp.task('default', gulp.series('clean', gulp.parallel(
  'transpile-src', 'transpile-scripts', 'copy-config', 'copy-node-config',
  'copy-public', 'copy-templates'
)));
