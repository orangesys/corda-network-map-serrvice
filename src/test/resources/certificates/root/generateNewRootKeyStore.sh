#!/bin/bash
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

keytool -genkeypair -keyalg EC -keysize 256 -alias ca -dname "CN=Root CA, OU=Cordite Foundation Network, O=Cordite Foundation, L=London, ST=London, C=GB" -ext bc:ca:true,pathlen:1 -ext bc:c -ext eku=serverAuth,clientAuth,anyExtendedKeyUsage -ext ku=digitalSignature,keyCertSign,cRLSign -keystore ca.jks -storepass changeme -keypass changeme -validity 1440

keytool -exportcert -rfc -alias ca -keystore ca.jks -storepass changeme -keypass changeme > ca.pem

keytool -genkeypair -keyalg EC -keysize 256 -alias cert -dname "CN=Root CA, OU=Cordite Foundation Network, O=Cordite Foundation, L=London, ST=London, C=GB" -ext bc:ca:true,pathlen:1 -ext bc:c -ext eku=serverAuth,clientAuth,anyExtendedKeyUsage -ext ku=digitalSignature,keyCertSign,cRLSign -keystore root.jks -storepass changeme -keypass changeme -validity 1440

keytool -genkeypair -keyalg EC -keysize 256 -alias key -dname "CN=Root CA, OU=Cordite Foundation Network, O=Cordite Foundation, L=London, ST=London, C=GB" -ext bc:ca:true,pathlen:1 -ext bc:c -ext eku=serverAuth,clientAuth,anyExtendedKeyUsage -ext ku=digitalSignature,keyCertSign,cRLSign -keystore root.jks -storepass changeme -keypass changeme -validity 1440

keytool -certreq -alias cert -keystore root.jks -storepass changeme -keypass changeme | keytool -gencert -ext eku=serverAuth,clientAuth,anyExtendedKeyUsage -ext bc:ca:true,pathlen:1 -ext bc:c -ext ku=digitalSignature,keyCertSign,cRLSign -rfc -keystore ca.jks -alias ca -storepass changeme -keypass changeme > cert.pem

keytool -certreq -alias key -keystore root.jks -storepass changeme -keypass changeme | keytool -gencert -ext eku=serverAuth,clientAuth,anyExtendedKeyUsage -ext bc:ca:true,pathlen:1 -ext bc:c -ext ku=digitalSignature,keyCertSign,cRLSign -rfc -keystore ca.jks -alias ca -storepass changeme -keypass changeme > key.pem

keytool -importcert -noprompt -file cert.pem -alias cert -keystore root.jks -storepass changeme -keypass changeme

keytool -importcert -noprompt -file key.pem -alias key -keystore root.jks -storepass changeme -keypass changeme

mv root.jks old.jks

keytool -importkeystore -srcstorepass changeme -srckeystore old.jks -deststorepass changeme -destkeystore old.p12 -deststoretype pkcs12

openssl pkcs12 -in old.p12 -out pemfile.pem -nodes -passin pass:changeme -passout pass:changeme

openssl pkcs12 -export -in pemfile.pem -name cert -out cert.p12 -passin pass:changeme -passout pass:changeme

openssl pkcs12 -export -in pemfile.pem -name key -out key.p12 -passin pass:changeme -passout pass:changeme

keytool -importkeystore -srcstorepass changeme -srckeystore cert.p12 -deststorepass changeme -destkeystore root.jks -srcstoretype pkcs12

keytool -importkeystore -srcstorepass changeme -srckeystore key.p12 -deststorepass changeme -destkeystore root.jks -srcstoretype pkcs12
