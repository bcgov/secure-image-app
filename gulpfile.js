const gulp = require('gulp');
const babel = require('gulp-babel');
const clean = require('gulp-clean');
const zip = require('gulp-zip');

gulp.task('clean', () => {
  return gulp.src('build', {
    read: false,
  })
    .pipe(clean({
      force: true,
    }));
});

gulp.task('transpile', ['clean'], () => {
  return gulp.src('server/**/*.js')
    .pipe(babel())
    .pipe(gulp.dest('build/server'));
});

gulp.task('copy-scripts', ['clean'], () => {
  return gulp.src('scripts/update-docker-npm.sh')
    .pipe(gulp.dest('build/scripts/update-docker-npm.sh'));
});

gulp.task('copy-config', ['clean'], () => {
  return gulp.src('server/config/*.json')
    .pipe(gulp.dest('build/server/config'));
});

gulp.task('copy-node-config', ['clean'], () => {
  return gulp.src(['package.json', 'package-lock.json'])
    .pipe(gulp.dest('build'));
});

gulp.task('copy-docker-config', ['clean'], () => {
  return gulp.src(['config/Dockerfile', 'config/Dockerrun.aws.json'])
    .pipe(gulp.dest('build'));
});

gulp.task('archive', ['transpile', 'copy-scripts', 'copy-config', 'copy-node-config',
  'copy-docker-config'], () => {
  return gulp.src('build/**', {
    dot: true,
  })
    .pipe(zip('archive.zip'))
    .pipe(gulp.dest('dist'));
});

gulp.task('default', ['clean', 'transpile', 'copy-config',
  'copy-node-config', 'copy-docker-config', 'archive'
]);
