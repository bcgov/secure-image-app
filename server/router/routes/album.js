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
/**
 * Write a ZIP `archive` to a temporary file
 *
 * @param {Object} archive An ZIP `archive` object
 * @returns Promose for pending operation
 */
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
/**
 * Check if a string consits of [Aa-Az] or [0-0] and is not undefined
 *
 * @param {String} str The string to be tested
 * @returns true if the string is valid, false otherwise
 */
function isValid(str) {
  return str && /^\w+$/.test(str);
}
/**
 * Create a ZIP `archive` with all the files from a bucket
 *
 * @param {String} bucketName The name of the bucket
 * @param {String} prefix Object prefix to filter bucket contents on
 * @param {boolean} [cleanup=true] If `true` archived objects will be removed when archived
 * @returns A `Promise` representing the pending operation
 */
async function archiveImagesInAlbum(bucketName, prefix, cleanup = true) {
  const objectsToRemove = [];
  const archive = archiver('zip', {
    zlib: {
      level: 6,
    }, // Sets the compression level.
  });
  const objects = await listBucket(bucket, `${prefix}/`);

  /* Expected object format
  { name: 'IMG_0112.jpg',
    lastModified: 2018-01-15T22:00:15.462Z,
    etag: 'c2435ac578f75ff9ab0c725e4b4c117c',
    size: 2000636 }
  */

  archive.on('error', (error) => {
    logger.log({ error }).error('Unable to create archive.');
  });

  let index = 0;
  // eslint-disable-next-line no-restricted-syntax
  for (const obj of objects) {
    // Fetch objects synchronously in case we have lots of objects that will
    // consume too much memory.
    // eslint-disable-next-line no-await-in-loop
    const buffer = await getObject(bucket, obj.name);

    archive.append(buffer, {
      name: `${archiveFileBaseName}${index}.jpg`,
    });
    index += 1;

    if (cleanup) {
      objectsToRemove.push(removeObject(bucket, obj.name));
    }
  }

  archive.finalize();

  await Promise.all(objectsToRemove);

  return Promise.resolve(archive);
}

/* eslint-disable */
/**
 * @api {POST} /albums/ Create a new album
 * @apiVersion 0.0.1
 * @apiName CreateAlbum
 * @apiGroup Albums
 *
 * @apiSuccess (200) {String}   id    The album's unique ID.
 *
 * @apiError   (401) Unauthorized     Authenticaiton required.
 * @apiError   (500) InternalError    The server encountered an internal error. Please retry the request.
 *
 * @apiExample {curl} Example usage:
 *  curl -X POST http://localhost:8000/v1/album/
 *
 * @apiSuccessExample Success-Response:
 *    HTTP/1.1 200 OK
 *    {
 *      "id": "d7995710-f665-11e7-8298-1b10696245bd"
 *    }
 *
 * @apiErrorExample {json} Error-Response:
 *    HTTP/1.1 401 Unauthorized
 *
 */
 /* eslint-enable */
router.post('/', asyncMiddleware(async (req, res) => {
  const albumId = uuid();

  logger.info(`Creating album with ID ${albumId}`);
  return res.status(200).json({ id: albumId });
}));

/* eslint-disable */
/**
 * @api {POST} /albums/:albumId Add a image to an album
 * @apiVersion 0.0.1
 * @apiName AddImageToAlbum
 * @apiGroup Albums
 *
 * @apiParam {String} {albumId}       The ID of the album that the image will be added to
 * @apiParam {String} {file}          The `Body` of the request must contain a multi-part mime encoded file object
 * 
 * @apiSuccess (200) {String} id      The image (object) unique ID
 *
 * @apiError   (401) Unauthorized     Authenticaiton required.
 * @apiError   (500) InternalError    The server encountered an internal error. Please retry the request.
 *
 * @apiExample {curl} Example usage:
 *  curl -X POST -v -F 'file=@IMG_0112.jpg' http://localhost:8000/v1/album/d7995710-f665-11e7-8298-1b10696245bd
 *
 * @apiSuccessExample Success-Response:
 *    HTTP/1.1 200 OK
 *    {
 *      "id": "9f1785b9c72a1c34138b7e0dbdb06c3a"
 *    }
 *
 * @apiErrorExample {json} Error-Response:
 *    HTTP/1.1 401 Unauthorized
 *
 */
 /* eslint-enable */
router.post('/:albumId', upload.single('file'), asyncMiddleware(async (req, res) => {
  const { albumId } = req.params;

  if (!req.file) {
    return res.status(400).json({ message: 'Unabe to process attached form.' });
  }

  /* This is the document format from multer:
    { fieldname: 'file',
    originalname: 'IMG_0112.jpg',
    encoding: '7bit',
    mimetype: 'image/jpeg',
    destination: 'uploads/',
    filename: '1f105fe3d6a937028056f545c83e13c0',
    path: 'uploads/1f105fe3d6a937028056f545c83e13c0',
    size: 2000636 }
  */

  const { filename } = req.file;
  const stream = fs.createReadStream(req.file.path);

  if (!stream) {
    return res.status(500).json({ message: 'Unable read attached file.' });
  }

  try {
    const etag = await putObject(bucket, path.join(albumId, filename), stream);
    if (etag) {
      fs.unlinkSync(req.file.path, (err) => {
        logger.log({ err }).error('Unable to unlink temprary file.');
      });
    }

    logger.info(`Adding image to album with name ${req.file.filename}, etag ${etag}`);

    return res.status(200).json({ id: req.file.filename });
  } catch (error) {
    logger.error(`Unable to put object, error ${error}`);
    return res.status(500).json({
      message: 'Unable to store attached file.',
    });
  }
}));

/* eslint-disable */
/**
 * @api {GET} /albums/:albumId Package the album into a archive file
 * @apiVersion 0.0.1
 * @apiName PackageAlbum
 * @apiGroup Albums
 *
 * @apiParam {String} {albumId}       The ID of the album that the image will be added to
 * 
 * @apiSuccess (200) {String} url     A `URL` to the downloadable archive
 *
 * @apiError   (401) Unauthorized     Authenticaiton required.
 * @apiError   (500) InternalError    The server encountered an internal error. Please retry the request.
 *
 * @apiExample {curl} Example usage:
 *  curl -X GET http://localhost:8000/v1/album/d7995710-f665-11e7-8298-1b10696245bd
 *
 * @apiSuccessExample Success-Response:
 *    HTTP/1.1 200 OK
 *    {
 *      "url": "http:/localhost:8000/download/57mq68m3cm7.zip"
 *    }
 *
 * @apiErrorExample {json} Error-Response:
 *    HTTP/1.1 401 Unauthorized
 *
 */
 /* eslint-enable */
router.get('/:albumId', asyncMiddleware(async (req, res) => {
  const archiveName = req.query.name;
  const { albumId } = req.params;

  if (archiveName && !isValid(archiveName)) {
    return res.status(400).json({ message: 'The optional archive name must contain letters or numbers.' });
  }

  const archive = await archiveImagesInAlbum(bucket, albumId);

  if (!archive) {
    return res.status(500).json({ message: 'Unable to archive album.' });
  }

  const file = await writeToTemporaryFile(archive);
  const stream = fs.createReadStream(file);

  if (!file || !stream) {
    return res.status(500).json({ message: 'Internal Error.' });
  }

  const fileName = `${albumId}/${archiveName || path.basename(file)}.zip`;
  const etag = await putObject(bucket, fileName, stream);

  // Cleanup the temorary archive file.
  fs.unlink(file);

  // If we don't have an etag the file was not written to the backing
  // store.
  if (!etag) {
    return res.status(500).json({ message: 'Unable to store image.' });
  }

  // Construct the download URI.
  const archiveFileName = `${path.basename(fileName)}`;
  const archiveFilePath = url.resolve(`v1/album/${albumId}/download/`, archiveFileName);
  const downloadUrl = url.resolve(config.get('appUrl'), archiveFilePath);

  logger.info(`Packaged album for download with URL ${downloadUrl}`);

  return res.status(200).json({ url: downloadUrl });
}));

/* eslint-disable */
/**
 * @api {GET} /:albumId/download/:fileName Download an album as a ZIP archive
 * @apiVersion  0.0.1
 * @apiName     DownloadAlbum
 * @apiGroup    Albums
 *
 * @apiParam {String} {albumId}       The ID of the album that the image will be added to
 * @apiParam {String} {fileName}      The name of the album archive file
 *
 * @apiSuccess (200) {Object}         The mime encoded binary representation of the archive
 *
 * @apiError   (401) Unauthorized     Authenticaiton required.
 * @apiError   (500) InternalError    The server encountered an internal error. Please retry the request.
 *
 * @apiExample {curl} Example usage:
 *  curl -X GET http://localhost:8000/v1/album/d7995710-f665-11e7-8298-1b10696245bd
 *
 * @apiSuccessExample Success-Response:
 *    HTTP/1.1 200 OK
 *
 * @apiErrorExample {json} Error-Response:
 *    HTTP/1.1 401 Unauthorized
 *
 */
 /* eslint-enable */
router.get('/:albumId/download/:fileName', asyncMiddleware(async (req, res) => {
  const { albumId, fileName } = req.params;
  const buffer = await getObject(bucket, path.join(albumId, fileName));

  if (!buffer) {
    return res.status(500).json({ message: 'Unable to fetch album archive.' });
  }

  res.contentType('application/octet-stream');

  logger.info(`Download album ZIP archive from bucket ${bucket}, path ${path.join(albumId, fileName)}`);

  return res.end(buffer, 'binary');
}));

module.exports = router;
