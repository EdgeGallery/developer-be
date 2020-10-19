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
FROM openjdk:8u201-jdk-alpine
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

RUN mkdir -p /home/mec-developer/user/workspace/csar/
# Create an app user so our program doesn't run as root.
RUN apk update &&\
    apk add shadow &&\
    groupadd -r -g $GID $GROUP_NAME &&\
    useradd -r -u $UID -g $GID -d $APP_HOME -s /sbin/nologin -c "Docker image user" $USER_NAME

WORKDIR $APP_HOME

COPY target/*.jar $APP_HOME/bin
COPY target/output $APP_HOME/bin
COPY configs/*.sh $APP_HOME/bin
COPY configs/mep_capability/ /home/mec-developer/mep_capability/

RUN chmod 750 $APP_HOME &&\
    chmod -R 550 $APP_HOME/bin &&\
    mkdir -p -m 750 $APP_HOME/config &&\
    mkdir -p -m 750 $APP_HOME/log &&\
    mkdir -p -m 750 $APP_HOME/packages &&\
    mkdir -p -m 700 $APP_HOME/ssl &&\
    chown -R $USER_NAME:$GROUP_NAME $APP_HOME


EXPOSE 9080

# Change to the app user.
USER $USER_NAME

# Execute script & application
ENTRYPOINT ["sh", "./bin/start.sh"]
#ENTRYPOINT ["sh", "-c"]
#CMD ["exec java -jar $APP_FILE"]
