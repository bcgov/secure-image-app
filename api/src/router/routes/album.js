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

import {
  logger,
  putObject,
  getObject,
  statObject,
  isExpired,
  asyncMiddleware,
  errorWithCode,
} from '@bcgov/nodejs-common-utils';
import shared from '../../libs/shared';
import { Router } from 'express';
import util from 'util';
import multer from 'multer';
import fs from 'fs';
import path from 'path';
import url from 'url';
import uuid from 'uuid/v1'; // timestamp based
import config from '../../config';
import { isValid } from '../../libs/utils';
import {
  writeToTemporaryFile,
  archiveImagesInAlbum,
} from '../../libs/archive';
import { NOTES_FILE_NAME } from '../../constants';
import { isAuthenticated } from '../../libs/auth';

const bucket = config.get('minio:bucket');
const upload = multer({ dest: config.get('temporaryUploadPath') });
const router = new Router();

/* eslint-disable */
/**
 * @api {POST} /album/ Create a new album
 * @apiVersion 0.0.1
 * @apiName CreateAlbum
 * @apiGroup Album
 *
 * @apiSuccess (200) {String}   id    The album's unique ID.
 *
 * @apiError   (401) Unauthorized     Authenticaiton required.
 * @apiError   (500) InternalError    The server encountered an internal error. Please retry the request.
 *
 * @apiExample {curl} Example
 *  curl -X POST http://localhost:8000/v1/album/
 *
 * @apiSuccessExample Success-Response
 *    HTTP/1.1 200 OK
 *    {
 *      "id": "d7995710-f665-11e7-8298-1b10696245bd"
 *    }
 *
 * @apiErrorExample {json} Error-Response
 *    HTTP/1.1 401 Unauthorized
 *
 */
 /* eslint-enable */
router.post('/', isAuthenticated, asyncMiddleware(async (req, res) => {
  const albumId = uuid();

  logger.info(`Creating album with ID ${albumId}`);
  return res.status(200).json({ id: albumId });
}));

/* eslint-disable */
/**
 * @api {POST} /album/:albumId Add a image to an album
 * @apiVersion 0.0.1
 * @apiName AddImageToAlbum
 * @apiGroup Album
 *
 * @apiParam {String} albumId         The ID of the album that the image will be added to
 * @apiParam {String} file            The `Body` of the request must contain a multi-part mime encoded file object
 * 
 * @apiSuccess (200) {String} id      The image MD5
 *
 * @apiError   (401) Unauthorized     Authenticaiton required.
 * @apiError   (500) InternalError    The server encountered an internal error. Please retry the request.
 *
 * @apiExample {curl} Example
 *  curl -X POST -v -F 'file=@IMG_0112.jpg' http://localhost:8000/v1/album/d7995710-f665-11e7-8298-1b10696245bd
 *
 * @apiSuccessExample Success-Response
 *    HTTP/1.1 200 OK
 *    {
 *      "id": "9f1785b9c72a1c34138b7e0dbdb06c3a"
 *    }
 *
 * @apiErrorExample {json} Error-Response
 *    HTTP/1.1 401 Unauthorized
 *
 */
 /* eslint-enable */
router.post('/:albumId', isAuthenticated, upload.single('file'), asyncMiddleware(async (req, res) => {
  const { albumId } = req.params;

  if (!req.file) {
    throw errorWithCode('Unable to process attached form.', 400);
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
    throw errorWithCode('Unable read attached file', 500);
  }

  try {
    const etag = await putObject(shared.minio, bucket, path.join(albumId, filename), stream);
    if (etag) {
      fs.unlinkSync(req.file.path, (err) => {
        logger.error(`Unable to unlink temprary file, error = ${err}`);
      });
    }

    logger.info(`Adding image to album with name ${req.file.filename}, etag ${etag}`);

    res.status(200).json({ id: etag });
  } catch (error) {
    if (error.code) {
      throw error;
    }

    logger.error(`Unable to put object, error ${error}`);
    throw errorWithCode('Unable to store attached file', 500);
  }
}));

/* eslint-disable */
/**
 * @api {GET} /album/:albumId Package the album into a archive file
 * @apiVersion 0.0.1
 * @apiName PackageAlbum
 * @apiGroup Album
 *
 * @apiParam {String} albumId         The ID of the album that the image will be added to
 * @apiParam {String} [name]          Preferred name for the archive
 * 
 * @apiSuccess (200) {String} url     A `URL` to the downloadable archive
 *
 * @apiError   (401) Unauthorized     Authenticaiton required.
 * @apiError   (500) InternalError    The server encountered an internal error. Please retry the request.
 *
 * @apiExample {curl} Default
 *  curl -X GET http://localhost:8000/v1/album/d7995710-f665-11e7-8298-1b10696245bd
 *
 * @apiSuccessExample Default
 *    HTTP/1.1 200 OK
 *    {
 *      "url": "http:/localhost:8000/download/57mq68m3cm7.zip"
 *    }
 *  @apiExample {curl} Archive Name
 *  curl -X GET http://localhost:8000/v1/album/d7995710-f665-11e7-8298-1b10696245bd?name=foo
 *
 * @apiSuccessExample Specifid Name
 *    HTTP/1.1 200 OK
 *    {
 *      "url": "http:/localhost:8000/download/foo.zip"
 *    }
 *
 * @apiErrorExample {json} Error-Response
 *    HTTP/1.1 401 Unauthorized
 *
 */
 /* eslint-enable */
router.get('/:albumId', isAuthenticated, asyncMiddleware(async (req, res) => {
  const archiveName = req.query.name;
  const {
    albumId,
  } = req.params;

  if (archiveName && !isValid(archiveName)) {
    throw errorWithCode('The optional archive name must contain letters or numbers', 400);
  }

  try {
    const archive = await archiveImagesInAlbum(bucket, albumId);
    if (!archive) {
      throw errorWithCode('Unable to archive album', 500);
    }

    const file = await writeToTemporaryFile(archive);
    const stream = fs.createReadStream(file);

    if (!file || !stream) {
      throw errorWithCode('Internal Error', 500);
    }

    const fileName = `${albumId}/${archiveName || path.basename(file)}.zip`;
    const etag = await putObject(shared.minio, bucket, fileName, stream);

    // TODO:(jl) Below here needs mocking and then integrations tests.

    // Cleanup the temporary archive file.
    const unlinkAsync = util.promisify(fs.unlink);
    await unlinkAsync(file);

    // If we don't have an etag the file was not written to the backing
    // store.
    if (!etag) {
      throw errorWithCode('Unable to store image', 500);
    }

    // Construct the download URI.
    const archiveFileName = `${path.basename(fileName)}`;
    const archiveFilePath = url.resolve(`v1/album/${albumId}/download/`, archiveFileName);
    const downloadUrl = url.resolve(config.get('appUrl'), archiveFilePath);

    logger.info(`Packaged album for download with URL ${downloadUrl}`);

    res.status(200).json({
      url: downloadUrl,
    });
  } catch (error) {
    if (error.code) {
      throw error;
    }

    const message = 'Unable to fetch album';
    logger.error(`${message}, error ${error}`);
    throw errorWithCode(message, 500);
  }
}));

/* eslint-disable */
/**
 * @api {POST} /album/:albumId/note Add field notes to an album
 * @apiVersion  0.0.1
 * @apiName     AddFieldNotes
 * @apiGroup    Album
 *
 * @apiParam {String} albumName   The name of the album
 * @apiParam {String} comment     The field notes
 *
 * @apiSuccess (200) {String} id      The field notes MD5
 *
 * @apiError   (401) Unauthorized     Authenticaiton required.
 * @apiError   (500) InternalError    The server encountered an internal error. Please retry the request.
 *
 * @apiExample {curl} Example
 *  curl -v -d "albumName=123" -X POST http://localhost:8000/v1/album/d7995710-f665-11e7-8298-1b10696245bd/note
 *
 * @apiSuccessExample Success-Response
 *    HTTP/1.1 201 OK
 *
 * @apiErrorExample {json} Error-Response
 *    HTTP/1.1 401 Unauthorized
 *
 */
 /* eslint-enable */
router.post('/:albumId/note', isAuthenticated, asyncMiddleware(async (req, res) => {
  const { albumName, comment } = req.body;
  const { albumId } = req.params;

  if (!albumName && !comment) {
    throw errorWithCode('All fields can not be empty', 400);
  }

  try {
    const notes = `Album Name: ${albumName || ''}\r\nComment: ${comment || ''}`;
    const buff = Buffer.from(notes, 'utf8');
    const name = path.join(albumId, NOTES_FILE_NAME);
    const etag = await putObject(shared.minio, bucket, name, buff);

    logger.info(`Adding field notes to album with name ${name}, etag ${etag}`);
    res.status(200).json({ id: etag });
  } catch (error) {
    if (error.code) {
      throw error;
    }

    logger.error(`Unable add notes to album, error ${error}`);
    throw errorWithCode('Unable to add notes to album', 500);
  }
}));

/* eslint-disable */
/**
 * @api {GET} /album/:albumId/download/:fileName Download an album as a ZIP archive
 * @apiVersion  0.0.1
 * @apiName     DownloadAlbum
 * @apiGroup    Album
 *
 * @apiParam {String} albumId         The ID of the album that the image will be added to
 * @apiParam {String} fileName        The name of the album archive file
 *
 * @apiSuccess (200) {Object}         The mime encoded binary representation of the archive
 *
 * @apiError   (401) Unauthorized     Authenticaiton required.
 * @apiError   (500) InternalError    The server encountered an internal error. Please retry the request.
 *
 * @apiExample {curl} Example
 *  curl -X GET http://localhost:8000/v1/album/d7995710-f665-11e7-8298-1b10696245bd
 *
 * @apiSuccessExample Success-Response
 *    HTTP/1.1 200 OK
 *
 * @apiErrorExample {json} Error-Response
 *    HTTP/1.1 401 Unauthorized
 *
 */
 /* eslint-enable */
router.get('/:albumId/download/:fileName', isAuthenticated, asyncMiddleware(async (req, res) => {
  const { albumId, fileName } = req.params;
  const albumExpirationInDays = config.get('albumExpirationInDays');

  try {
    const stat = await statObject(shared.minio, bucket, `${albumId}/${fileName}`);

    if (isExpired(stat, albumExpirationInDays)) {
      const baseUrl = config.get('appUrl');
      const redirectTo = '/expired.html';
      const redirectUrl = url.resolve(baseUrl, redirectTo);

      res.redirect(301, redirectUrl); // 301 Moved Permanently
    }
  } catch (error) {
    const message = 'Unable to retrieve album';
    logger.error(`${message}, err = ${error.message}`);
    throw errorWithCode(`${message}`, 400);
  }

  try {
    const buffer = await getObject(shared.minio, bucket, path.join(albumId, fileName));

    if (!buffer) {
      throw errorWithCode('Unable to fetch album archive', 500);
    }

    res.contentType('application/octet-stream');

    logger.info(`Download album ZIP archive from bucket ${bucket}, path ${path.join(albumId, fileName)}`);

    res.end(buffer, 'binary');
  } catch (error) {
    if (error.code) {
      throw error;
    }

    const message = 'Unable to retrieve album';
    logger.error(`${message}, err = ${error.message}`);
    throw errorWithCode(message, 500);
  }
}));

module.exports = router;
