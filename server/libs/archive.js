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
// Created by Jason Leach on 2018-02-01.
//

/* eslint-env es6 */

'use strict';

import { getObject, listBucket, logger, removeObject } from '@bcgov/common-nodejs';
import archiver from 'archiver';
import fs from 'fs';
import path from 'path';
import config from '../config';

const archiveFileBaseName = config.get('archiveFileBaseName');
const tempFilePath = '/tmp';

/**
 * Write a ZIP `archive` to a temporary file
 *
 * @param {Object} archive An ZIP `archive` object
 * @returns Promose for pending operation
 */
export const writeToTemporaryFile = archive => new Promise((resolve, reject) => {
  const tempFileName = Math.random().toString(36).slice(2);
  const file = path.join(tempFilePath, tempFileName);
  const output = fs.createWriteStream(file);

  output.on('close', () => {
    logger.info(`${archive.pointer()} total bytes`);
    logger.info('archiver has been finalized and the output file descriptor has closed.');

    resolve(file);
  });

  output.on('error', (err) => {
    reject(err);
  });

  archive.pipe(output);
});

/**
 * Create a ZIP `archive` with all the files from a bucket
 *
 * @param {String} bucketName The name of the bucket
 * @param {String} prefix Object prefix to filter bucket contents on
 * @param {boolean} [cleanup=true] If `true` archived objects will be removed when archived
 * @returns A `Promise` representing the pending operation
 */
export const archiveImagesInAlbum = async (bucketName, prefix, cleanup = true) => {
  const objectsToRemove = [];
  const archive = archiver('zip', {
    zlib: {
      level: 6,
    }, // Sets the compression level.
  });
  const objects = await listBucket(bucketName, `${prefix}/`);

  /* Expected object format
  { name: 'IMG_0112.jpg',
    lastModified: 2018-01-15T22:00:15.462Z,
    etag: 'c2435ac578f75ff9ab0c725e4b4c117c',
    size: 2000636 }
  */

  archive.on('error', (error) => {
    logger.error(`Unable to create archive, error = ${error}`);
  });

  let index = 0;
  // eslint-disable-next-line no-restricted-syntax
  for (const obj of objects) {
    // Fetch objects synchronously in case we have lots of objects that will
    // consume too much memory.
    // eslint-disable-next-line no-await-in-loop
    const buffer = await getObject(bucketName, obj.name);

    let name;
    if (obj.name.indexOf('.txt') !== -1) {
      name = path.basename(obj.name);
    } else {
      name = `${archiveFileBaseName}${index}.jpg`;
    }

    archive.append(buffer, {
      name,
    });
    index += 1;

    if (cleanup) {
      objectsToRemove.push(removeObject(bucketName, obj.name));
    }
  }

  archive.finalize();

  await Promise.all(objectsToRemove);

  return Promise.resolve(archive);
};
