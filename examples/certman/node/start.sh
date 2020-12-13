#!/bin/sh
#
#   Copyright 2018, Cordite Foundation.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

# Starts Cordite node. Creates node.conf if missing

set -e

echo "  _____            ___ __     
 / ___/__  _______/ (_) /____ 
/ /__/ _ \\/ __/ _  / / __/ -_)
\\___/\\___/_/  \\_,_/_/\\__/\\__/"

if [ -f ./build-info.txt ]; then
   cat build-info.txt
fi

# Variables used to create node.conf, defaulted if not set
CORDITE_LEGAL_NAME=${CORDITE_LEGAL_NAME:=O=Cordite-$(od -x /dev/urandom | head -1 | awk '{print $7$8$9}'), OU=Cordite, L=London, C=GB}
CORDITE_P2P_ADDRESS=${CORDITE_P2P_ADDRESS:=localhost:10002}
# CORDITE_DB_DIR see below for defaulting
CORDITE_COMPATIBILITY_ZONE_URL=${CORDITE_COMPATIBILITY_ZONE_URL:=https://network-map-test.cordite.foundation}
CORDITE_KEY_STORE_PASSWORD=${CORDITE_KEY_STORE_PASSWORD:=cordacadevpass}
CORDITE_TRUST_STORE_PASSWORD=${CORDITE_TRUST_STORE_PASSWORD:=trustpass}
CORDITE_DB_USER=${CORDITE_DB_USER:=sa}
CORDITE_DB_PASS=${CORDITE_DB_PASS:=dbpass}
CORDITE_BRAID_PORT=${CORDITE_BRAID_PORT:=8080}
CORDITE_DEV_MODE=${CORDITE_DEV_MODE:=true}
CORDITE_DETECT_IP=${CORDITE_DETECT_IP:=false}


# Create node.conf if it does not exist and default if variables not set
if [ ! -f ./node.conf ]; then
  echo "./node.conf not found, creating"
  basedir=\"\${baseDirectory}\"
  braidhost=${CORDITE_LEGAL_NAME#*O=} && braidhost=${braidhost%%,*} && braidhost=$(echo $braidhost | sed 's/ //g')
cat > node.conf <<EOL
myLegalName : "${CORDITE_LEGAL_NAME}"
p2pAddress : "${CORDITE_P2P_ADDRESS}"
compatibilityZoneURL : "${CORDITE_COMPATIBILITY_ZONE_URL}"
dataSourceProperties : {
    dataSourceClassName : org.h2.jdbcx.JdbcDataSource
    "dataSource.url" : "jdbc:h2:file:${CORDITE_DB_DIR:=${basedir}/db}/persistence"
    "dataSource.user" : ${CORDITE_DB_USER}
    "dataSource.password" : ${CORDITE_DB_PASS}
}
keyStorePassword : ${CORDITE_KEY_STORE_PASSWORD}
trustStorePassword : ${CORDITE_TRUST_STORE_PASSWORD}
devMode : ${CORDITE_DEV_MODE}
detectPublicIp: ${CORDITE_DETECT_IP}
jvmArgs : [ "-Dbraid.${braidhost}.port=${CORDITE_BRAID_PORT}" ]
EOL
fi

# Configure notaries
if [ ! -z "$CORDITE_NOTARY" ] ; then
echo "CORDITE_NOTARY set to ${CORDITE_NOTARY}. Configuring node to be a notary"
cat >> node.conf <<EOL
notary {
    validating=${CORDITE_NOTARY}
}
EOL
fi

if [ ! -z "$CORDITE_METERING_CONFIG" ] ; then
   echo "CORDITE_METERING_CONFIG set to ${CORDITE_METERING_CONFIG}. Creating metering-service-config.json"
   echo $CORDITE_METERING_CONFIG > metering-service-config.json
fi

if [ ! -z "$CORDITE_FEE_DISPERSAL_CONFIG" ] ; then
   echo "CORDITE_FEE_DISPERSAL_CONFIG set to ${CORDITE_FEE_DISPERSAL_CONFIG}. Creating fee-dispersal-service-config.json"
   echo $CORDITE_FEE_DISPERSAL_CONFIG > fee-dispersal-service-config.json
fi

# start Cordite Node, if in docker container use CMD from docker to allow override
if [ -f /.dockerenv ]; then
    "$@"
else
    java -jar corda.jar --log-to-console --no-local-shell
fi

