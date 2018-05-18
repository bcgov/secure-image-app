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
// Created by Jason Leach on 2018-05-19.
//

'use strict';

import config from '../server/config';
import { expiredTopLevelObjects } from '../server/libs/bucket';
import { logger } from '../server/libs/logger';

const bucket = config.get('minio:bucket');
const albumExpirationInDays = config.get('albumExpirationInDays');

const main = async () => {
  try {
    const prefix = '';
    const old = await expiredTopLevelObjects(bucket, prefix, albumExpirationInDays);
    const promises = old.map(o => removeObject(bucket, o.prefix));

    await Promise.all(promises);

    let message;
    if (promises.length === 0) {
      message = 'No objects to prune.';
    } else {
      message = `Pruned ${promises.length} objects and related contents`;
      logger.info(message);
    }

    process.exit();
  } catch (error) {
    const message = 'There was a error purning old container objects';
    logger.error(`${message}, err = ${error.message}`);

    process.exit(1);
  }
};

main();
