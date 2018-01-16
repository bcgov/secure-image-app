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

import * as minio from 'minio';
import config from '../config';

const client = new minio.Client({
  endPoint: config.get('minio:endPoint'),
  port: config.get('minio:port'),
  secure: config.get('minio:secure'),
  accessKey: config.get('minio:accessKey'),
  secretKey: config.get('minio:secretKey'),
});

// eslint-disable-next-line import/prefer-default-export
export const listBucket = (bucket, prefix = '') => new Promise((resolve, reject) => {
  const stream = client.listObjectsV2(bucket, prefix, false);
  const objects = [];

  stream.on('data', (obj) => {
    objects.push(obj);
  });

  stream.on('end', () => {
    resolve(objects);
  });

  stream.on('error', (error) => {
    reject(error);
  });
});

// data and be a buffer or stream
export const putObject = (bucket, name, data) => new Promise((resolve, reject) => {
  client.putObject(bucket, name, data, (error, etag) => {
    if (error) {
      reject(error);
    }

    resolve(etag);
  });
});

export const getObject = (bucket, name) => new Promise((resolve, reject) => {
  let size = 0;
  const data = [];

  client.getObject(bucket, name, (error, stream) => {
    if (error) {
      reject(error);
    }

    stream.on('data', (chunk) => {
      size += chunk.length;
      data.push(chunk);
    });

    stream.on('end', () => {
      resolve(Buffer.concat(data, size));
    });

    stream.on('error', (serror) => {
      reject(serror);
    });
  });
});

export const getPresignedUrl = (bucket, name) => new Promise((resolve, reject) => {
  const expiryInSeconds = config.get('minio:expiry');

  client.presignedUrl('GET', bucket, name, expiryInSeconds, (error, presignedUrl) => {
    if (error) {
      reject(error);
    }

    resolve(presignedUrl);
  });
});
