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

const assert = require('assert');
const expect = require('chai').expect;
const minio = require('minio');
const sinon = require('sinon');
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

const object = {
  name: 'IMG_0112.jpg',
  lastModified: new Date(),
  etag: 'c2435ac578f75ff9ab0c725e4b4c117c',
  size: 2000636
}

describe('minio bucket helpers', function() {

  beforeEach(function() {
    // nothig to do
  });

  it('listBucket returns an array of JSON objects', async () => { // mochaAsync(async () => {
    const stream = new PassThrough()
    stream.end();

    const stub = sinon.stub(minio.Client.prototype, 'listObjectsV2').returns(stream);

    bucket.client = stub;

    const objects = await bucket.listBucket('foo', 'bar');

    stream.emit('data', object);
    stream.emit('data', object);

    expect(objects).to.be.instanceof(Array);  // return type array
    expect(objects).to.contain(object);       // contents are JSON
    expect(objects.length).to.equal(2);       // count is OK
  });
});
