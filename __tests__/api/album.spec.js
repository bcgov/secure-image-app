
//
// Code Signing
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
// Created by Jason Leach on 2018-07-20.
//

import path from 'path';
import { default as request } from 'supertest'; // eslint-disable-line
import app from '../../src';

const sample = path.join(__dirname, 'sample.zip');

describe('Test album routes', () => {
  test('Create album should fail with a bad token', async () => {
    const response = await request(app)
      .post('/v1/album')
      .set('Authorization', 'Bearer 123cake123')
      .set('Content-Type', 'application/json')
      .expect(401)
      .expect('Content-Type', /json/);
  });

  test('Create album should provide an album ID', async () => {
    const response = await request(app)
      .post('/v1/album')
      .set('Authorization', 'Bearer 123bacon123')
      .set('Content-Type', 'application/json')
      .expect(200)
      .expect('Content-Type', /json/);
    expect(response.body.id).not.toBeUndefined();
  });

  test('Add file to album should fail with a bad token', async () => {
    const response = await request(app)
      .post('/v1/album/abc123')
      .set('Authorization', 'Bearer 123cake123')
      .set('Content-Type', 'application/json')
      .attach('file', sample)
      .expect(401)
      .expect('Content-Type', /json/);
  });

  test('Add file to album should fail with no file attachment', async () => {
    await request(app)
      .post('/v1/album/abc123')
      .set('Authorization', 'Bearer 123bacon123')
      .expect(400)
      .expect('Content-Type', /json/);
  });

  test('Add file to album should succeed when all parameters are valid', async () => {
    const response = await request(app)
      .post('/v1/album/abc123')
      .set('Authorization', 'Bearer 123bacon123')
      .attach('file', sample)
      .expect(200)
      .expect('Content-Type', /json/);
      expect(response.body.id).not.toBeUndefined();
  });
});
