# Copyright 2018, Cordite Foundation.

#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at

#    http://www.apache.org/licenses/LICENSE-2.0

#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

FROM amazonlinux:2

LABEL VENDOR="Cordite.foundation" \
      MAINTAINER="devops@cordite.foundation"

EXPOSE 8080

ENV NMS_TLS=false
ENV NMS_DOORMAN=true
ENV NMS_CERTMAN=true
ENV NMS_PORT=8080
ENV NMS_DB=db
ENV NMS_MONGO_CONNECTION_STRING=mongodb://localhost:27017
#ENV NMS_ROOT_CA_FILE_PATH=certs/root.jks
#ENV NMS_MONGOD_LOCATION=/opt/cordite/mongodb-linux-x86_64-amazon2-4.0.4/bin/mongod

WORKDIR /opt/cordite

RUN yum install -y java-1.8.0-openjdk-headless shadow-utils.x86_64 \
  && yum clean all \
  && mkdir -p /opt/cordite/db /opt/cordite/logs /home/cordite \
  && groupadd -g 1000 -r cordite \
  && useradd -r -u 1000 -g cordite cordite \
  && chgrp -R 0 /opt/cordite \
  && chmod -R g=u /opt/cordite \
  && chown -R cordite:cordite /opt/cordite /home/cordite

USER cordite

VOLUME /opt/cordite/db /opt/cordite/logs /opt/cordite/certs

COPY target/network-map-service/lib/ /opt/cordite/lib/
COPY target/network-map-service/network-map-service.jar /opt/cordite/network-map-service.jar

CMD ["/usr/bin/java", "-cp", "/opt/cordite/network-map-service.jar:/opt/cordite/lib/*", "io.cordite.networkmap.NetworkMapApp"]
