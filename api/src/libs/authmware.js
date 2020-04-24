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
// Created by Jason Leach on 2018-02-14.
//

/* eslint-env es6 */

'use strict';

import express from 'express';
import session from 'express-session';
import passport from 'passport';
import { Strategy as OAuth2Strategy } from 'passport-oauth2';
import url from 'url';
import config from '../config';

const authmware = (app) => {
  const sessionOptions = {
    secret: config.get('session:key'),
    cookie: {
      maxAge: config.get('session:maxAge'),
      httpOnly: false,
    },
    resave: false,
    saveUninitialized: false,
  };

  app.use(session(sessionOptions));
  app.use(passport.initialize());
  app.use(passport.session());

  // We don't store any user information.
  passport.serializeUser((user, done) => {
    done(null, {});
  });

  // We don't load any addtional user information.
  passport.deserializeUser((id, done) => {
    done(null, {});
  });

  // We don't use the credentials for anything, just the isAuthenticated() in
  // the session object to confifm authentication.

  const oAuth2Strategy = new OAuth2Strategy(
    {
      authorizationURL: config.get('sso:authUrl'),
      tokenURL: config.get('sso:tokenUrl'),
      clientID: config.get('sso:clientId'),
      clientSecret: config.get('sso:clientSecret'),
      callbackURL: url.resolve(`${config.get('appUrl')}`, config.get('sso:callback')),
    },
    (accessToken, refreshToken, profile, done) => done(null, {}),
  );

  // eslint-disable-next-line arrow-body-style
  oAuth2Strategy.authorizationParams = () => {
    // eslint-disable-next-line camelcase
    return { kc_idp_hint: 'idir' };
  };

  passport.use(oAuth2Strategy);
};

module.exports = () => {
  const app = express();
  authmware(app);

  return app;
};
