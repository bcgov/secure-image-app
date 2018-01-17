FROM node:8.9.4

RUN apt-get update
RUN npm install -g npm

# Create a non-root user
RUN groupadd -r nodejs \
    && useradd -m -r -g nodejs nodejs

USER nodejs

# Create a home for the application
RUN mkdir -p /home/nodejs/app
WORKDIR /home/nodejs/app

# Install app dependencies
COPY package.json /home/nodejs/app
COPY package-lock.json /home/nodejs/app
RUN npm install --produciton

# Bundle app source
COPY server /home/nodejs/app/server

ENV NODE_PATH /home/nodejs/app/server

EXPOSE 8100

CMD ["npm", "run", "dev"]
