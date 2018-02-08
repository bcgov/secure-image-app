import {
  logger,
} from './logger';

const request = require('request');

// This will be picked up by express error handleing middleware.
function errorHandler(err) {
  logger.info(err.message);
  const error = new Error('Unauthorized');
  error.code = 401;
  throw error;
}

const authenticate = () => new Promise((resolve, reject) => {
  request.post('https://dev-sso.pathfinder.gov.bc.ca/auth/realms/mobile/protocol/openid-connect/token/', {
    form: {
      grant_type: 'client_credentials',
      client_id: 'secure-image-api',
      client_secret: '0e6e0200-d003-46bd-8f5c-035736e94e1f',
    },
  }, (err, res, apiAuthBody) => {
    if (err) {
      reject(err);
      return;
    }

    const apiAccessToken = JSON.parse(apiAuthBody).access_token;
    if (apiAccessToken == null) {
      const error = new Error('No api access_token');
      error.code = 401;
      reject(error);
      return;
    }

    logger.info('Api access token success');
    resolve(apiAccessToken);
  });
});

const introspection = (apiAccessToken, clientAccessToken) => new Promise((resolve, reject) => {
  request.post('https://dev-sso.pathfinder.gov.bc.ca/auth/realms/mobile/protocol/openid-connect/token/introspect', {
    form: {
      grant_type: 'client_credentials',
      client_id: 'secure-image-api',
      client_secret: '0e6e0200-d003-46bd-8f5c-035736e94e1f',
    },
  }, (err, res, introspectionBody) => {
    if (err) {
      reject(err);
      return;
    }

    const errorDescription = JSON.parse(introspectionBody).error_description;
    if (errorDescription != null) {
      const error = new Error(errorDescription);
      error.code = 401;
      reject(error);
      return;
    }

    resolve();
  });
});

// eslint-disable-next-line import/prefer-default-export
export const isAuthenticated = (req, res, next) => {
  // CHECK THE USER STORED IN SESSION FOR A CUSTOM VARIABLE
  // you can do this however you want with whatever variables you set up
  const clientAccessToken = req.header('access_token');
  if (clientAccessToken != null) {
    authenticate()
      .then((apiAccessToken) => {
        console.log('a here');
        return introspection(apiAccessToken, clientAccessToken);
      })
      .then(() => {
        console.log('b here');
        next(null, true);
      })
      .catch((err) => {
        console.log('e here');
        res.status(401).json({ error: err.message });
        // errorHandler(err);
      });
    return;
  }

  errorHandler(new Error('No access token passed'));
};
