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

import { asyncMiddleware, logger, errorWithCode } from '@bcgov/nodejs-common-utils';
import { Router } from 'express';
import passport from 'passport';
import url from 'url';
import config from '../../config';
import { TEMPLATES } from '../../constants';
import { compile, loadTemplate } from '../../libs/template';

const router = new Router();

/* eslint-disable */
/**
 * @api {GET} /auth/login Begin the authentication workflow
 * @apiVersion 0.0.1
 * @apiName Login
 * @apiGroup Authentication
 * @apiDescription This API is meant to start the authentication workflow for browser based
 * clients. Don't use this from a script / program.
 */
 /* eslint-enable */
router.get('/login', passport.authenticate('oauth2'));

/* eslint-disable */
/**
 * @api {GET} /auth/callback Complete the authentication worlflow
 * @apiVersion 0.0.1
 * @apiName Callback
 * @apiGroup Authentication
 * @apiDescription This API is meant for oAuth2 providers use for callbacks as
 * specified in the protocol. Don't use this from a script / program.
 */
 /* eslint-enable */
router.get('/callback', passport.authenticate('oauth2', {
  failureRedirect: '/failed.html',
}), asyncMiddleware(async (req, res) => {
  const redirectTo = req.session.redirect_to;
  const baseUrl = config.get('appUrl');
  try {
    const buffer = await loadTemplate(TEMPLATES.DOWNLOAD);
    console.log('u=', url.resolve(baseUrl, redirectTo));
    const html = await compile(buffer, {
      download_url: url.resolve(baseUrl, redirectTo),
    });

    res.send(html);
  } catch (err) {
    const message = `Unable to build download template: ${TEMPLATES.DOWNLOAD}`
    logger.error(`message, error = ${err.message}`);
    
    throw errorWithCode(message, 500);
  }
}));

module.exports = router;
