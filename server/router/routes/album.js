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

import { Router } from 'express';
import multer from 'multer';
import fs from 'fs';
import path from 'path';
import url from 'url';
import uuid from 'uuid/v1'; // timestamp based
import archiver from 'archiver';
import ip from 'ip';
import config from '../../config';
import {
  logger,
} from '../../libs/logger';
import {
  asyncMiddleware,
} from '../../libs/utils';
import {
  putObject,
  listBucket,
  getObject,
  removeObject,
} from '../../libs/bucket';

const bucket = config.get('minio:bucket');
const archiveFileBaseName = config.get('archiveFileBaseName');
const temporaryUploadPath = config.get('temporaryUploadPath');
const tempFilePath = '/tmp';
const router = new Router();
const upload = multer({ dest: temporaryUploadPath });

function writeToTemporaryFile(archive) {
  return new Promise((resolve, reject) => {
    const tempFileName = Math.random().toString(36).slice(2);
    const file = path.join(tempFilePath, tempFileName);
    const output = fs.createWriteStream(file);

    output.on('close', () => {
      logger.log().info(`${archive.pointer()} total bytes`);
      logger.log().info('archiver has been finalized and the output file descriptor has closed.');

      resolve(file);
    });

    output.on('error', (err) => {
      reject(err);
    });

    archive.pipe(output);
  });
}

// Create a new album
router.post('/', asyncMiddleware(async (req, res) => {
  logger.log('POST /');
  return res.status(200).json({ id: uuid() });
}));

// Add a document (image) to an item
router.post('/:albumId', upload.single('file'), asyncMiddleware(async (req, res) => {
  logger.log('POST /:albumId');

  /*
    { fieldname: 'file',
    originalname: 'IMG_0112.jpg',
    encoding: '7bit',
    mimetype: 'image/jpeg',
    destination: 'uploads/',
    filename: '1f105fe3d6a937028056f545c83e13c0',
    path: 'uploads/1f105fe3d6a937028056f545c83e13c0',
    size: 2000636 }
  */

  const { albumId } = req.params;
  const { filename } = req.file;
  const stream = fs.createReadStream(req.file.path);
  const etag = await putObject(bucket, path.join(albumId, filename), stream);

  fs.unlinkSync(req.file.path);

  if (etag) {
    return res.status(200).json({ id: req.file.filename });
  }

  return res.status(500).json();
}));

// Get the download URL for an album
router.get('/:albumId', asyncMiddleware(async (req, res) => {
  logger.log('GET /:albumId');

  const cleanup = [];
  const { albumId } = req.params;
  const archive = archiver('zip', {
    zlib: {
      level: 6,
    }, // Sets the compression level.
  });

  archive.on('error', (error) => {
    console.log('fail', error);
    // logger.log({ error }).error('unable to create archive.');
  });

  /*
  { name: 'IMG_0112.jpg',
    lastModified: 2018-01-15T22:00:15.462Z,
    etag: 'c2435ac578f75ff9ab0c725e4b4c117c',
    size: 2000636 }
  */

  const objects = await listBucket(bucket, `${albumId}/`);

  let index = 0;
  // eslint-disable-next-line no-restricted-syntax
  for (const obj of objects) {
    // eslint-disable-next-line no-await-in-loop
    const buffer = await getObject(bucket, obj.name);

    archive.append(buffer, {
      name: `${archiveFileBaseName}${index}.jpg`,
    });
    index += 1;

    // eslint-disable-next-line no-await-in-loop
    cleanup.push(removeObject(bucket, obj.name));
  }

  archive.finalize();
  await Promise.all(cleanup);

  const file = await writeToTemporaryFile(archive);
  const stream = fs.createReadStream(file);
  const archiveName = `${albumId}/${path.basename(file)}.zip`;
  const etag = await putObject(bucket, archiveName, stream);

  if (!etag) {
    return res.status(500);
  }

  fs.unlinkSync(file);

  const port = config.get('port');
  const host = `http://${ip.address()}:${port}`;
  const archiveFileName = `${path.basename(file)}.zip`;
  const archiveFilePath = url.resolve(`v1/album/${albumId}/download/`, archiveFileName);
  const downloadUrl = url.resolve(host, archiveFilePath);

  return res.status(200).json({ url: downloadUrl });
}));

// Download the album as a ZIP archive
router.get('/:albumId/download/:fileName', asyncMiddleware(async (req, res) => {
  logger.log('GET /:albumId/download/:fileName');

  const { albumId, fileName } = req.params;
  const buffer = await getObject(bucket, path.join(albumId, fileName));

  res.contentType('application/octet-stream');

  return res.end(buffer, 'binary');
}));

module.exports = router;
