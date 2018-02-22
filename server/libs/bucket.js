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
import {
  logger,
} from './logger';

const client = new minio.Client({
  endPoint: config.get('minio:endPoint'),
  port: config.get('minio:port'),
  secure: config.get('minio:secure'),
  accessKey: config.get('minio:accessKey'),
  secretKey: config.get('minio:secretKey'),
  region: config.get('minio:region'),
});

export const makeBucket = bucket => new Promise((resolve, reject) => {
  client.makeBucket(bucket, (err) => {
    if (err) {
      reject(err);
      return;
    }

    resolve();
  });
});

export const bucketExists = bucket => new Promise((resolve, reject) => {
  // The API for `bucketExists` does not seem to match the documentaiton. In
  // returns an error with code 'NoSuchBucket' if a bucket does *not* exists;
  // the docs say no error should be retunred and success should equal false.
  client.bucketExists(bucket, (err) => {
    if (err && err.code === 'NoSuchBucket') {
      resolve(false);
      return;
    }

    // Any other error is a legit error.
    if (err && err.code !== 'NoSuchBucket') {
      reject(err);
      return;
    }

    resolve(true);
  });
});

/**
 * List contest of a bucket.
 *
 * @param {String} bucket The name of the bucket.
 * @param {String} [prefix=''] Prefix to filter the contents on.
 */
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

/**
 * Add an object to a bucket
 *
 * @param {String} bucket The name of the bucket
 * @param {String} name The name the object will have in the bucket
 * @param {Buffer} data The object data `Stream` or `Buffer`
 */
export const putObject = (bucket, name, data) => new Promise((resolve, reject) => {
  client.putObject(bucket, name, data, (error, etag) => {
    if (error) {
      reject(error);
    }

    resolve(etag);
  });
});

/**
 * Fetch an object from an existing bucket
 *
 * @param {String} bucket The name of the bucket
 * @param {String} name The name of the object to retrieve
 */
export const getObject = (bucket, name) => new Promise((resolve, reject) => {
  let size = 0;
  const data = [];

  client.getObject(bucket, name, (error, stream) => {
    if (error) {
      reject(error);
      return;
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

/**
 * Get a resigned URL for an object
 *
 * @param {String} bucket The name of the bucket
 * @param {String} name The name of the object
 */
export const getPresignedUrl = (bucket, name) => new Promise((resolve, reject) => {
  const expiryInSeconds = config.get('minio:expiry');

  client.presignedUrl('GET', bucket, name, expiryInSeconds, (error, presignedUrl) => {
    if (error) {
      reject(error);
    }

    resolve(presignedUrl);
  });
});

/**
 * Remove an object from a bucket
 *
 * @param {String} bucket The name of the bucket
 * @param {String} name The name of the object
 */
export const removeObject = (bucket, name) => new Promise((resolve, reject) => {
  client.removeObject(bucket, name, (error) => {
    if (error) {
      reject(error);
    }

    resolve();
  });
});

// HELPERS

export const createBucketIfRequired = async (bucket) => {
  try {
    const exists = await bucketExists(bucket);
    if (!exists) {
      await makeBucket(bucket);
    }
  } catch (err) {
    logger.error(`Unable to create bucket: ${bucket}, error = ${err}`);
    throw new Error(`Unable to create bucket: ${bucket}`);
  }
};
