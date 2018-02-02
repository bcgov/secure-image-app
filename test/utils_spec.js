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

import jest from 'jest';
import { isValid } from '../server/libs/utils'

describe('utility helpers', function() {

    beforeEach(() => {
      // nothig to do
    });
  
    afterEach(() => {
      // nothig to do
    });
  
    test('isValid handles a valid string', async () => {
        let testString = 'a-b_c%123';

        expect(isValid(testString)).toBe(true); 
    });

    test('isValid handles string with invalid characters', async () => {
        let testString = 'a-b_c#123';

        expect(isValid(testString)).toBe(false); 
    });
});
