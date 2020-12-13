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

openssl dgst -sha256 -sign domain.key domain.crt | base64 | cat domain.crt - | curl -k -X POST -d @- http://localhost:8080/certman/api/generate -o download/keys.zip
pushd download
rm -rf *.jks
unzip keys.zip
popd
rm -rf node/certificates/*
cp download/*.jks node/certificates/

