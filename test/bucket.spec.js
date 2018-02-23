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
    // nothing to do
  });

  test('listBucket returns an array of JSON objects', async () => {
    const stream = new PassThrough()
    const stub = sinon.stub(minio.Client.prototype, 'listObjectsV2').returns(stream);

    stream.end();

    bucket.client = stub;

    const objects = await bucket.listBucket('aBucketName', 'aObjectName');

    stream.emit('data', object1);
    stream.emit('data', object1);

    expect(typeof objects).toBe('object'); // return type array
    expect(objects.length).toEqual(2); // count is OK
    expect(objects[0]).toEqual(object1); // contents are JSON   
  });

  test('putObject returns a JSON object with etag property or Error', async () => {
    const stub = sinon.stub(minio.Client.prototype, 'putObject');
    const error = new Error('Hello World');
    stub.onFirstCall().yields(undefined, object2);
    stub.onSecondCall().yields(error, undefined);

    bucket.client = stub;

    const result = await bucket.putObject('aBucketName', 'aObjectName', new Buffer(32));

    expect(typeof result).toBe('object'); // return type array
    expect(result).toEqual(object2); // contents are JSON   
    expect(result).toHaveProperty('etag') // has correct props

    await expect(bucket.putObject('aBucketName', 'aObjectName', new Buffer(32))).rejects.toEqual(error);
  });

  test('getObject returns a Buffer of expected data', async () => {
    const str1 = 'hello world';
    const str2 = 'hello jello';
    const stream = new PassThrough()
    const stub = sinon.stub(minio.Client.prototype, 'getObject')

    stream.push(str1);
    stream.push(str2);
    stream.end();

    stub.onFirstCall().yields(undefined, stream);

    bucket.client = stub;

    const result = await bucket.getObject('aBucketName', 'aObjectName');

    expect(typeof result).toBe('object'); // return type object
    expect(result.length).toEqual((str1 + str2).length); // size is OK
  });

  test('getPresignedUrl returns URL in string format or Error', async () => {
    const url = 'http://example.com/';
    const stub = sinon.stub(minio.Client.prototype, 'presignedUrl');
    const error = new Error('Hello World');
    stub.onFirstCall().yields(undefined, url);
    stub.onSecondCall().yields(error, undefined);

    bucket.client = stub;

    const result = await bucket.getPresignedUrl('aBucketName', 'aObjectName');

    expect(typeof result).toBe('string'); // return type array
    expect(result).toEqual(url); // contents are string   

    await expect(bucket.getPresignedUrl('aBucketName', 'aObjectName')).rejects.toEqual(error);
  });

  test('removeObject retunrs correctly for success and Error', async () => {
    const stub = sinon.stub(minio.Client.prototype, 'removeObject');    
    const error = new Error('Hello World');
    stub.onFirstCall().yields(undefined, undefined);
    stub.onSecondCall().yields(error, undefined);

    bucket.client = stub;

    const result = await bucket.removeObject('aBucketName', 'aObjectName');

    // success
    expect(typeof result).toBe('undefined');

    // fails
    await expect(bucket.removeObject('aBucketName', 'aObjectName')).rejects.toEqual(error);
  });

  test.skip('makeBucket creates a bucket', async () => {

  });

  test('bucketExists correctly determins the existance of a bucket', async () => { 
    const stub = sinon.stub(minio.Client.prototype, 'bucketExists');
    const error1 = new Error('Hello World');
    error1.code = 'NoSuchBucket'
    const error2 = new Error('Hello World');
    error2.code = 'ZuluFoxtrotAlpha'

    bucket.client = stub;

    stub.onFirstCall().yields(error1, undefined);
    stub.onSecondCall().yields(error2, undefined);

    // success
    expect(await bucket.bucketExists('aBucketName')).toBe(false);

    // fails
    await expect(bucket.bucketExists('aBucketName')).rejects.toEqual(error2);
  });

  test.skip('bucketExists correctly determins an error state', async () => { 

  });
});
