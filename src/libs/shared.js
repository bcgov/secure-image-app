//
// Code Sign
//
// Copyright © 2018 Province of British Columbia
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
// Created by Jason Leach on 2018-07-23.
//

'use strict';

import * as minio from 'minio';
import config from '../config';

const key = Symbol.for('ca.bc.gov.pathfinder.secure-image.minio');
const gs = Object.getOwnPropertySymbols(global);

if (!(gs.indexOf(key) > -1)) {
  global[key] = new minio.Client({
    endPoint: config.get('minio:host'),
    port: config.get('minio:port'),
    useSSL: config.get('minio:secure'),
    accessKey: config.get('minio:accessKey'),
    secretKey: config.get('minio:secretKey'),
    region: config.get('minio:region'),
  });
}

const singleton = {};

Object.defineProperty(singleton, 'minio', {
  get: () => global[key],
});

Object.freeze(singleton);

export default singleton;
