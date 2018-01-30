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

import * as minio from 'minio';
import jest from 'jest';
import sinon from 'sinon';

const bucket = require('../server/libs/bucket');
const PassThrough = require('stream').PassThrough;

// const mochaAsync = (fn) => {
//   return async () => {
//     try {
//       await fn();
//     } catch (err) {
//       throw err;
//     }
//   };
// };

const object1 = {
  name: 'IMG_0112.jpg',
  lastModified: new Date(),
  etag: 'c2435ac578f75ff9ab0c725e4b4c117c',
  size: 2000636
}

const object2 = {
  etag: '12345'
}

describe('minio bucket helpers', function() {

  beforeEach(() => {
    // nothig to do
  });

  afterEach(() => {
    // nothig to do
  });

  test('listBucket returns an array of JSON objects', async () => {
    const stream = new PassThrough()
    stream.end();

    const stub = sinon.stub(minio.Client.prototype, 'listObjectsV2').returns(stream);

    bucket.client = stub;

    const objects = await bucket.listBucket('foo', 'bar');

    stream.emit('data', object1);
    stream.emit('data', object1);

    expect(typeof objects).toBe('object'); // return type array
    expect(objects.length).toEqual(2); // count is OK
    expect(objects[0]).toEqual(object1); // contents are JSON   
  });

  test('putObject returns a JSON object with etag property or throws', async () => {
    const stub = sinon.stub(minio.Client.prototype, 'putObject');
    const error = new Error('Hello World');
    stub.onFirstCall().yields(undefined, object2);
    stub.onSecondCall().yields(error, undefined);

    bucket.client = stub;

    const result = await bucket.putObject('foo', 'bar', new Buffer(32));

    expect(typeof result).toBe('object'); // return type array
    expect(result).toEqual(object2); // contents are JSON   
    expect(result).toHaveProperty('etag') // has correct props

    await expect(bucket.putObject('foo', 'bar', new Buffer(32))).rejects.toEqual(error);
  });
});
