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

// This is a sample integration test. To complete API testing we need setup some
// test data and figure out how to stub out the minio storage so we don't fill up
// the servers storage every time a test is run.

import chai from 'chai';
import chttp from 'chai-http';
import server from '../server';

let should = chai.should();

chai.use(chttp);

// process.env.NODE_ENV = 'test';

describe('API', () => {
  it('POST / should a new album', (done) => {
    chai.request.agent(server)
      .post('/v1/album')
      .end((err, res) => {
        res.should.have.status(200);
        res.body.should.be.a('object');
        res.body.should.have.property('id');
        done();
      });
  });
});
