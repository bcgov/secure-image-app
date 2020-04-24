FROM openjdk:latest

# Create an ANDROID_HOME and install the SDK tools
RUN cd /tmp && \
    curl https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip -O && \
    mkdir -p /opt/android && \
    unzip -q -d /opt/android sdk-tools-linux-3859397.zip && \
    rm -f sdk-tools-linux-3859397.zip && \
    cd -

ENV ANDROID_HOME=/opt/android

# Install the build tools, agree to the licensing.
RUN mkdir -p /root/.android/ && \
    touch /root/.android/repositories.cfg && \
    /opt/android/tools/bin/sdkmanager --update && \
    yes | /opt/android/tools/bin/sdkmanager --licenses
