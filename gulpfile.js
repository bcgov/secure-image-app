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
const zip = require('gulp-zip');

gulp.task('clean', () => gulp.src('build', { read: false })
  .pipe(clean({
    force: true,
  })));

gulp.task('transpile', ['clean'], () => gulp.src('server/**/*.js')
  .pipe(babel())
  .pipe(gulp.dest('build/server')));

gulp.task('copy-config', ['clean'], () => gulp.src('server/config/*.json')
  .pipe(gulp.dest('build/server/config')));

gulp.task('copy-node-config', ['clean'], () => gulp.src(['package.json', 'package-lock.json'])
  .pipe(gulp.dest('build')));

gulp.task('copy-docker-config', ['clean'], () => gulp.src(['config/Dockerfile'])
  .pipe(gulp.dest('build')));

gulp.task('archive', ['transpile', 'copy-config', 'copy-node-config',
  'copy-docker-config'], () => gulp.src('build/**', { dot: true })
  .pipe(zip('archive.zip'))
  .pipe(gulp.dest('dist')));

gulp.task('default', ['clean', 'transpile', 'copy-config',
  'copy-node-config', 'copy-docker-config', 'archive',
]);
