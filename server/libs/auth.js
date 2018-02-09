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

import request from 'request';
import jwt from 'jsonwebtoken';
import pemFromModAndExponent from 'rsa-pem-from-mod-exp';
import config from '../config';

function sendError(res, statusCode, message) {
  res.status(statusCode).json({ error: message, success: false });
}

const verifyToken = clientAccessToken => new Promise((resolve, reject) => {
  request.get(config.get('authCertsEndpoint'), {

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
  const authHeader = req.headers.authorization;
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
