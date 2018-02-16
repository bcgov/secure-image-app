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
// Created by Jason Leach on 2018-01-15.
//

/* eslint-env es6 */

'use strict';

import fs from 'fs';
import path from 'path';
import handlebars from 'handlebars';
import config from '../config';

export const loadTemplate = fileName => new Promise((resolve, reject) => {
  const fpath = path.join(__dirname, '../../', config.get('templates:path'), `${fileName}.html`);

  fs.access(fpath, fs.constants.R_OK, (accessErr) => {
    if (accessErr) {
      reject(accessErr);
      return;
    }

    fs.readFile(fpath, 'utf8', (readErr, data) => {
      if (readErr) {
        reject(readErr);
      }

      resolve(data);
    });
  });
});

export const compile = (source, data) => {
  const template = handlebars.compile(source);
  return Promise.resolve(template(data));
};
