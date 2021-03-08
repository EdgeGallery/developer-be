#
#    Copyright 2020 Huawei Technologies Co., Ltd.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#
FROM swr.ap-southeast-1.myhuaweicloud.com/edgegallery/openjdk:8u201-jdk-alpine
#define all environment variable here
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV TZ='Asia/Shanghai'
ENV APP_FILE mec-developer-platform.jar
ENV APP_HOME /usr/app
ENV UID=166
ENV GID=166
ENV USER_NAME=eguser
ENV GROUP_NAME=eggroup
ENV ENV="/etc/profile"

# # CREATE APP USER ##
# Set umask
RUN sed -i "s|umask 022|umask 027|g" /etc/profile

# Create the home directory for the new app user.
RUN mkdir -p /usr/app
RUN mkdir -p /usr/app/bin
RUN mkdir -p /usr/app/lib

# Create an app user so our program doesn't run as root.
RUN apk update &&\
    apk add shadow &&\
    groupadd -r -g $GID $GROUP_NAME &&\
    useradd -r -u $UID -g $GID -d $APP_HOME -s /sbin/nologin -c "Docker image user" $USER_NAME

RUN apk add docker

WORKDIR $APP_HOME

RUN chmod -R 750 $APP_HOME &&\
    chmod -R 550 $APP_HOME/bin &&\
    mkdir -p -m 750 $APP_HOME/config &&\
    mkdir -p -m 750 $APP_HOME/log &&\
    mkdir -p -m 700 $APP_HOME/ssl &&\
    mkdir -p -m 750 $APP_HOME/user &&\
    mkdir -p -m 750 $APP_HOME/user/app &&\
    mkdir -p -m 750 $APP_HOME/user/plugin &&\
    mkdir -p -m 750 $APP_HOME/user/workspace &&\
    mkdir -p -m 750 $APP_HOME/user/workspace/csar &&\
    mkdir -p -m 750 $APP_HOME/user/uploaded_files &&\
    mkdir -p -m 750 $APP_HOME/mep_capability &&\
    chown -R $USER_NAME:$GROUP_NAME $APP_HOME &&\
    chown -R $USER_NAME:$GROUP_NAME $APP_HOME/log &&\
    chown -R $USER_NAME:$GROUP_NAME $APP_HOME/user &&\
    chown -R $USER_NAME:$GROUP_NAME $APP_HOME/user/workspace &&\
    chown -R $USER_NAME:$GROUP_NAME $APP_HOME/user/uploaded_files

COPY --chown=$USER_NAME:$GROUP_NAME target/*.jar $APP_HOME/bin
COPY --chown=$USER_NAME:$GROUP_NAME target/output $APP_HOME
COPY --chown=$USER_NAME:$GROUP_NAME configs/mep_capability $APP_HOME/mep_capability
COPY --chown=$USER_NAME:$GROUP_NAME lib/*.jar  $APP_HOME/lib/

EXPOSE 9080

# Change to the app user.
USER $USER_NAME

# Execute script & application
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar ./bin/$APP_FILE"]
