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

import path from 'path';

import fs from 'fs';
import express from 'express';
import cookieParser from 'cookie-parser';
import bodyParser from 'body-parser';
import flash from 'connect-flash';
import {
  logger,
  started,
} from './libs/logger';
import config from './config';
import authmw from './libs/authmware';

const env = config.get('environment');

// Middlewares

// Config
const isDev = env !== 'production';
const port = config.get('port');
const app = express();
const options = {
  inflate: true,
  limit: '3000kb',
  type: 'image/*',
};
const docpath = path.join(__dirname, '../', 'public/doc/api');
const pubpath = path.join(__dirname, '../', 'public');

fs.access(docpath, fs.constants.R_OK, (err) => {
  if (err) {
    logger.warn('API documentation does not exist');
    return;
  }

  app.use('/doc', express.static(docpath));
});

fs.access(pubpath, fs.constants.R_OK, (err) => {
  if (err) {
    logger.warn('Static assets location does not exist');
    return;
  }

  app.use('/', express.static(pubpath));
});

app.use(cookieParser());
app.use(bodyParser.urlencoded({
  extended: true,
}));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true,
}));
app.use(bodyParser.raw(options));
app.use(flash());
// app.use('/download', express.static('download'));

// Authentication middleware
app.use(authmw(app));

// Server API routes
require('./router')(app);

// Error handleing middleware. This needs to be last in or it will
// not get called.
app.use((err, req, res, next) => { // eslint-disable-line no-unused-vars
  logger.error(err.message);
  const code = err.code ? err.code : 500;
  const message = err.message ? err.message : 'Internal Server Error';

  res.status(code).json({ error: message, success: false });
});

app.listen(port, '0.0.0.0', (err) => {
  if (err) {
    return logger.error(`There was a problem starting the server, ${err.message}`);
  }
  if (isDev) {
    return started(port);
  }
  return logger.info(`Production server running on port: ${port}`);
});

module.exports = app;
