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
/* eslint-disable no-console */

'use strict';

import chalk from 'chalk';
import ip from 'ip';
import winston from 'winston';

const divider = chalk.gray('\n-----------------------------------');
const options = {};

// When testing log to a file for further analysis.
if (process.env.NODE_ENV === 'test') {
  options.transports = [
    new (winston.transports.File)({ filename: 'test_run.log' }),
  ];
} else {
  options.transports = [
    new (winston.transports.Console)(),
  ];
}

/**
 * Re-export logger object
 *
 */
export const logger = new (winston.Logger)(options);

/**
 * Print canned message when the server starts
 *
 * @param {String} port The port the server is running on
 */
export const started = (port) => {
  console.log(`${chalk.cyan('\nProbateapp')} API started ${chalk.green('✓')}`);
  console.log(`${chalk.bold('\nAccess URLs:')}${divider}
               \nLocalhost: ${chalk.magenta(`http://localhost:${port}`)}
               \r      LAN: ${chalk.magenta(`http://${ip.address()}:${port}`)}
               ${divider}`);

  if (process.env.NODE_ENV === 'development') {
    console.log(`${chalk.blue(`\nPress ${chalk.italic('CTRL-C')} to stop\n`)}`);
  }
};
