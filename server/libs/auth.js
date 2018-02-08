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
  
  const error = new Error('Something went wrong');
  error.code = 401;
  reject(error);
  
  // resolve(true);
  
  // request.post('https://dev-sso.pathfinder.gov.bc.ca/auth/realms/mobile/protocol/openid-connect/token/introspect', {
  //   form: {
  //     grant_type: 'client_credentials',
  //     client_id: 'secure-image-api',
  //     client_secret: '0e6e0200-d003-46bd-8f5c-035736e94e1f',
  //   },
  // }, (err, res, introspectionBody) => {
  //   if (err) {
  //     reject(err);
  //     return;
  //   }

  //   const errorDescription = JSON.parse(introspectionBody).error_description;
  //   if (errorDescription != null) {
  //     const error = new Error(errorDescription);
  //     error.code = 401;
  //     reject(error);
  //     return;
  //   }

  //   resolve();
  // });
});

// eslint-disable-next-line import/prefer-default-export
export const isAuthenticated = async (req, res, next) => {
  // if (!req.headers.authorization) {
  //   return res.status(403).json({ error: 'No credentials sent.' });
  // }

  const clientAccessToken = ''; // funciton to parse out token....

  try {
    const apiAccessToken = await authenticate();
    const results = await introspection(apiAccessToken, clientAccessToken);

    if (results) {
      next();
    }
  } catch (err) {
    logger.error(`authentication error = ${err.message}`);

    return res.status(401).json({ error: err.message, success: false });

    // const error = new Error('Unauthorized');
    // error.code = 401;
    // throw error;
  }
};
