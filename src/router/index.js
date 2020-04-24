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

import cors from 'cors';
import config from '../config';
import album from './routes/album';
import auth from './routes/auth';
import ehlo from './routes/ehlo';

const corsOptions = {
  origin: config.get('appUrl'),
  credentials: true,
  optionsSuccessStatus: 200, // some legacy browsers (IE11, various SmartTVs) choke on 204
};

module.exports = (app) => {
  app.use(cors(corsOptions));
  app.use('/ehlo', ehlo); // probes
  app.use('/v1/auth', auth);
  app.use('/v1/album', album);
};
