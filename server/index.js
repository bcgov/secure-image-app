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

import express from 'express';
import cookieParser from 'cookie-parser';
import bodyParser from 'body-parser';
import flash from 'connect-flash';
import {
  logger,
  started,
} from './libs/logger';
import config from './config';

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

app.use(cookieParser());
app.use(bodyParser.urlencoded({
  extended: true,
}));
app.use(bodyParser.json());
app.use(bodyParser.raw(options));
app.use(flash());
// app.use('/download', express.static('download'));

// Server API routes
require('./router')(app);

// Error handleing middleware. This needs to be last in or it will
// not get called.
app.use((err, req, res, next) => { // eslint-disable-line no-unused-vars
  logger.log(err).error(err.message);

  const code = err.code ? err.code : 500;
  const message = err.message ? err.message : 'Internal Server Error';

  res.status(code).json({ error: message, success: false });
});

app.listen(port, '0.0.0.0', (err) => {
  if (err) {
    return logger.log(err).error('There was a problem starting the server');
  }
  if (isDev) {
    return started(port);
  }
  return logger.log(`Production server running on port: ${port}`);
});

module.exports = app;
