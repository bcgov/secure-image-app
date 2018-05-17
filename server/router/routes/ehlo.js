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
// Created by Jason Leach on 2018-01-18.
//

/* eslint-env es6 */

'use strict';

import { Router } from 'express';
import { asyncMiddleware } from '../../libs/utils';

const router = new Router();

/* eslint-disable */
/**
 * @api {GET} /ehlo Check if the server is alive and well
 * @apiVersion 0.0.1
 * @apiName HelloPing
 * @apiGroup ehlo
 * 
 * @apiSuccess (200)  Sucesfull ping
 *
 * @apiExample {curl} Example usage:
 *  curl -X GET http://localhost:8000/v1/ehlo
 *
 * @apiSuccessExample Success-Response:
 *    HTTP/1.1 200 OK
 *
 * @apiErrorExample {json} Error-Response:
 *    HTTP/1.1 500 InternalError
 *
 */
 /* eslint-enable */
router.get('/', asyncMiddleware(async (req, res) => res.status(200).end()));

module.exports = router;
