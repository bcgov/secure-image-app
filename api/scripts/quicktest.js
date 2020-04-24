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
// Created by Jason Leach on 2018-07-30.
//

'use strict';

const util = require('util');
const fs = require('fs');
const request = require('request-promise-native');

const readFile = util.promisify(fs.readFile);
const token = 'eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJqUVV4MXktSG14aG9KU2NOckhSWUJPMXdzRkVUY05kVDdhTFUxTW1kc3VjIn0.eyJqdGkiOiJkZWVkNzRiOS1hMDY0LTQxNDgtYjNmNi01NzgwN2I5ZThkMmQiLCJleHAiOjE1MzI5ODg4MzUsIm5iZiI6MCwiaWF0IjoxNTMyOTg3MDM1LCJpc3MiOiJodHRwczovL2Rldi1zc28ucGF0aGZpbmRlci5nb3YuYmMuY2EvYXV0aC9yZWFsbXMvbW9iaWxlIiwiYXVkIjoic2VjdXJlLWltYWdlIiwic3ViIjoiNmZkYmM0YTktYWI4MC00MzRkLWJkM2MtYmUwYWVhZGUzN2Y1IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic2VjdXJlLWltYWdlIiwiYXV0aF90aW1lIjoxNTMyOTg1MDMzLCJzZXNzaW9uX3N0YXRlIjoiMThiMGIyOTYtYWIzMy00NWY4LWI4NjAtOTIzMDIxNzE1NmIwIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6W10sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InJlYWxtLW1hbmFnZW1lbnQiOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJyZWFsbS1hZG1pbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJxdWVyeS1jbGllbnRzIiwicXVlcnktdXNlcnMiLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyIsInF1ZXJ5LWdyb3VwcyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwibmFtZSI6Ikphc29uIExlYWNoIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiamxlYWNoX28iLCJnaXZlbl9uYW1lIjoiSmFzb24iLCJmYW1pbHlfbmFtZSI6IkxlYWNoIiwiZW1haWwiOiJqYXNvbi5sZWFjaEBmdWxsYm9hcmNyZWF0aXZlLmNvbSJ9.LmGBJrwd4SYMc-SHlOM7bOylm5C1MoriXoTvhMyFakUEKmSjstlqAZGCZQ09K_ccWIm2H5awymg4VUi4a5eg3ivYPy0Cd1aL3ZJOnynfrAgAV6VU3YALa4k45lzh2i7RrtAOtEPJKECtI9lhJSOUdGhngpw9YWf6KZJZOFYtHgb2S6JKJF2Co2FwioCKMUjqguDZZKasTmCzR4iunJXiCn3TmPGhdAUdchHp3EJM3Cs3E0cIB6w1Fl3cHI8QvYOPhYb76Jbvtl4DBKzn7Obitg-GWP7oEomF3RbleArDL7Gz-CEshIa6JB-FK6klZuaFeFhjXs6MvG_5Sasmdl6q0A';
const sampleFilePath = 'IMG_0279.jpg';

const main = async () => {

  try {
    const album = await request({
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      method: 'POST',
      uri: 'http://localhost:8000/v1/album',
      json: true,
    });
    console.log(`created album = ${album.id}`)
    for (let i = 0; i < 3; i++) {
      const receipt = await request({
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        url: `http://localhost:8000/v1/album/${album.id}`,
        method: 'POST',
        json: true,
        formData: {
          'file': {
            value: await readFile(sampleFilePath),
            options: {
              filename: 'file'
            }
          }
        }
      });
      console.log(`file receipt = ${receipt.id}`);
    }

    const albumDetails = await request({
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      method: 'GET',
      uri: `http://localhost:8000/v1/album/${album.id}`,
      json: true,
    });

    console.log(`download URL = ${albumDetails.url}`);

    // console.log(albumDetails.url);
    // const stream = await request({
    //   headers: {
    //     'Authorization': `Bearer ${token}`,
    //   },
    //   method: 'GET',
    //   uri: albumDetails.url,
    // }).pipe(fs.createWriteStream('x.zip'));
    // // stream.on('finish', async () => { console.log('done'); });

  } catch (err) {
    console.log('xxxx', err.message);
  }
}

main();
