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

/**
 * Helper function to wrap express rountes to handle rejected promises
 *
 * @param {Function} fn The `next()` function to call
 */
export const asyncMiddleware = fn =>
  // Make sure to `.catch()` any errors and pass them along to the `next()`
  // middleware in the chain, in this case the error handler.
  (req, res, next) => {
    Promise.resolve(fn(req, res, next))
      .catch(next);
  };

  /**
 * Check if a string consits of [Aa-Az], [0-9], -, _, and %.
 *
 * @param {String} str The string to be tested
 * @returns true if the string is valid, false otherwise
 */
export const isValid = str => str && /^[0-9A-Za-z\s\-_%]+$/.test(str);
