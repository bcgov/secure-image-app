import {
  logger,
} from './logger';

const request = require('request');

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
      reject(new Error('No api access_token'));
      return;
    }

    resolve(apiAccessToken);
  });
});

const introspection = (apiAccessToken, clientAccessToken) => new Promise((resolve, reject) => {
  logger.info(`api token ${apiAccessToken}`);
  logger.info(`client token ${clientAccessToken}`);
  request.post('https://dev-sso.pathfinder.gov.bc.ca/auth/realms/mobile/protocol/openid-connect/token/introspect', {
    auth: {
      bearer: apiAccessToken,
    },
    form: {
      token: clientAccessToken,
    },
  }, (err, res, introspectionBody) => {
    if (err) {
      reject(err);
      return;
    }

    const errorDescription = JSON.parse(introspectionBody).error_description;
    if (errorDescription) {
      logger.info(errorDescription);
      reject(new Error(errorDescription));
      return;
    }

    resolve();
  });
});

function sendError(res, statusCode, message) {
  res.status(statusCode).json({ error: message, success: false });
}

// eslint-disable-next-line import/prefer-default-export
export const isAuthenticated = async (req, res, next) => {
  const clientAccessToken = req.headers.authorization;
  if (!clientAccessToken) {
    return sendError(res, 403, 'No credentials sent.');
  }

  try {
    const apiAccessToken = await authenticate();
    await introspection(apiAccessToken, clientAccessToken);
    next();
  } catch (err) {
    return sendError(res, 401, err.message);
  }

  return sendError(res, 500, 'Auth request not handled');
};
