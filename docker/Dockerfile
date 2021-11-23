# Copyright 2020-2021 Huawei Technologies Co., Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM swr.cn-north-4.myhuaweicloud.com/eg-common/openjdk:8u201-jre-alpine

# Define all environment variable here
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV TZ='Asia/Shanghai'
ENV APP_FILE appstore.jar
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

# Create an app user so our program doesn't run as root.
RUN apk update &&\
    apk add shadow &&\
    groupadd -r -g 166 eggroup &&\
    useradd -r -u 166 -g 166 -d /usr/app -s /sbin/nologin -c "Docker image user" eguser

RUN apk add docker
WORKDIR /usr/app

RUN chmod 750 /usr/app &&\
    chmod -R 550 /usr/app/bin &&\
    mkdir -p -m 750 /usr/app/config &&\
    mkdir -p -m 750 /usr/app/log &&\
    mkdir -p -m 750 /usr/app/packages &&\
    mkdir -p -m 700 /usr/app/ssl &&\
    chown -R eguser:eggroup /usr/app

COPY --chown=eguser:eggroup target/*.jar /usr/app/bin

EXPOSE 8099

# Change to the app user.
USER eguser

# Execute script & application
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar ./bin/appstore.jar"]
