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

import jwt from 'jsonwebtoken';
import request from 'request';
import pemFromModAndExponent from 'rsa-pem-from-mod-exp';
import config from '../config';
import { logger } from './logger';

const sendError = (res, statusCode, message) => {
  logger.info(`Rejecting authenticaiton, message = ${message}`);
  res.status(statusCode).json({ error: message, success: false });
};

const verifyToken = clientAccessToken => new Promise((resolve, reject) => {
  request.get(config.get('sso:certsUrl'), {

  }, (err, res, certsBody) => {
    if (err) {
      reject(err);
      return;
    }

    const certsJson = JSON.parse(certsBody).keys[0];
    const modulus = certsJson.n;
    const exponent = certsJson.e;
    const algorithm = certsJson.alg;

    if (!modulus) {
      reject(new Error('No modulus'));
      return;
    }

    if (!exponent) {
      reject(new Error('No exponent'));
      return;
    }

    if (!algorithm) {
      reject(new Error('No algorithm'));
      return;
    }

    // build a certificate
    const pem = pemFromModAndExponent(modulus, exponent);

    // verify
    jwt.verify(clientAccessToken, pem, { algorithms: [algorithm] }, (verifyErr, verifyResult) => {
      if (verifyErr) {
        reject(verifyErr);
        return;
      }
      resolve(verifyResult);
    });
  });
});

// eslint-disable-next-line import/prefer-default-export
export const isAuthenticated = async (req, res, next) => {
  // The download URL requires that the user authenticates via their
  // browser which will add an 'isAuthenticated' method for testing.
  if (/^.*\/album\/[0-9A-Za-z-]*\/download\/.*$/.test(req.originalUrl)) {
    logger.info('Verifying web user authentication');
    // This is not the same isAuthenticated() as above.
    if (req.isAuthenticated()) {
      return next();
    }

    logger.info('Redirecting web user to login');
    req.session.redirect_to = req.originalUrl;
    res.redirect('/v1/auth/login');
    return null;
  }

  // Other API will use a Bearer JWT and should be verified
  // as follows.
  logger.info('Verifying API user authentication');

  const authHeader = req.headers.authorization;
  if (authHeader == null) {
    return sendError(res, 400, 'Please send Authorization header');
  }

  const authHeaderArray = authHeader.split(' ');
  if (authHeaderArray.length < 2) {
    return sendError(res, 400, 'Please send Authorization header with bearer type first followed by a space and then access token');
  }
  const clientAccessToken = authHeaderArray[1];

  try {
    const verifyResult = await verifyToken(clientAccessToken);
    if (verifyResult) {
      return next();
    }
    return sendError(res, 401, 'Invalid or expired access token');
  } catch (err) {
    return sendError(res, 401, err.message);
  }
};
