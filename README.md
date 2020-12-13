> As the maintainers of Cordite Network Map Service (NMS), we value every community member's positive contribution of time and effort. We made a choice to open source the code making it free and available for everyone. As the use of Cordite NMS grows so does the demands for support and maintenance. We have introduced a Cordite subscription to be able to provide support and maintenance to those organisations who are reliant on Cordite. The subscription will prioritise your issues and answer your calls for support. It will also accelerate the roadmap and delivery of your feature requests. If you are intending to use Cordite in production we would recommend your company evaluates the value of a Cordite subscription. If this is of interest to you then please drop an email to community@cordite.foundation 

## Contents

- [Supported tags and respective Dockerfile links](#supported-tags-and-respective-dockerfile-links)
- [Design criteria and Features](#design-criteria-and-features)
- [Design Wiki](https://gitlab.com/cordite/network-map-service/wikis/design)
- [Backlog of Features, Improvements, and Optimisations](#backlog-of-features--improvements--and-optimisations)
- [FAQ](FAQ.md)
- [How do I get in touch?](#how-do-i-get-in-touch)
- [What if something does not work?](#what-if-something-does-not-work)
- [How do I contribute?](#how-do-i-contribute-)
- [Who is behind the Network Map Service?](#who-is-behind-the-network-map-service-)
- [What open source license has this been released under?](#what-open-source-license-has-this-been-released-under)
- [How do I start a simple network on my local workstation or laptop?](#how-do-i-start-a-simple-network-on-my-local-workstation-or-laptop)
- [Command line parameters](#command-line-parameters)
- [Doorman protocol](#doorman-protocol)
  * [Retrieving the NetworkMap `network-map-truststore.jks`](#retrieving-the-networkmap--network-map-truststorejks)
- [Certman protocol](#certman-protocol)
- [Releasing NMS](#releasing-nms)
- [Contributors](#contributors)
- [License](#license)

## Supported tags and respective Dockerfile links
* `v0.5.2` `latest` - latest stable release
* `edge` - latest master build, unstable

## Design criteria and Features
1. Meet the requirements of the [Corda Network Map Service protocol](https://docs.corda.net/network-map.html), both documented and otherwise
2. A nominal implementation of the Doorman protocol
3. A new protocol, [_Certman_](#certman-protocol), for registration with client-provided certificates
4. Completely stateless - capable of running in load-balanced clusters
5. Efficient use of I/O to serve 5000+ concurrent read requests per second from a modest server
6. Transparent filesystem design to simplify maintenance, backup, and testing

## Backlog of Features, Improvements, and Optimisations

See our issues board for what this networkmap can't do as yet:
https://gitlab.com/cordite/network-map-service/boards

>>>
The following are on the roadmap but not implemented yet:

1. Keys are not stored in a HSM and crypto operations are executed in-process.
2. The doorman protocol implementation doesn't have the necessary KYC human workflow - all CSRs to the doorman are automatically accepted.
3. The integration tests do not cover HA configurations for either the service or the database.
4. There are several features of the API that as yet have not been surfaced to the front-end and hence the front-end is limited for production admin dashboard.
5. The admin access control is not integrated with typical ACL services used by enterprise organisations (e.g. ActiveDirectory etc).

It is recommended that these amoungst other things would be need for this software to be suitable for production.  
If you would like to accelerate the roadmap and use this software in production then feel free to either contribute or get in contact on the #cordite channel on [Corda slack](https://slack.corda.net/) 
>>>

## How do I get in touch?
  + News is announced on [@We_are_Cordite](https://twitter.com/we_are_cordite)
  + More information can be found on [Cordite website](https://cordite.foundation)
  + We use #cordite channel on [Corda slack](https://slack.corda.net/) 
  + We informally meet at the [Corda London meetup](https://www.meetup.com/pro/corda/)

## What if something does not work?
We encourage you to raise any issues/bugs you find in Cordite. Please follow the below steps before raising issues:
   1. Check on the [Issues backlog](https://gitlab.com/cordite/network-map-service/issues) to make sure an issue on the topic has not already been raised
   2. Post your question on the #cordite channel on [Corda slack](https://slack.corda.net/)
   3. If none of the above help solve the issue, [raise an issue](https://gitlab.com/cordite/network-map-service/issues/new?issue) following the contributions guide

## How do I contribute?
We welcome contributions both technical and non-technical with open arms! There's a lot of work to do here. The [Contributing Guide](https://gitlab.com/cordite/network-map-service/blob/master/contributing.md) provides more information on how to contribute.

## Who is behind the Network Map Service?
Network Map Service is being developed by a group of financial services companies, software vendors and open source contributors. The project is hosted on here on GitLab. 

## What open source license has this been released under?
All software in this repository is licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## How do I start a simple network on my local workstation or laptop?

Using Docker:

```bash
docker run -e NMS_ROOT_CA_FILE_PATH="" -p 8080:8080 cordite/network-map:latest
```

You can configure the service using `-D` system properties. See the section for 
[command line parameters](#command-line-parameters).

See the detailed instructions in the [FAQ](FAQ.md#1-show-me-how-to-set-up-a-simple-network) for more details and option.

## Command line parameters

Java properties (pass with -D<propertyname>=<property-value>) and env variables:

| Property                      | Env Variable                      | Default                                                                                            | Description                                                                                                           |
| ----------------------------  | --------------------------------  | -------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| auth-password                 | NMS_AUTH_PASSWORD                 | admin                                                                                              | system admin password                                                                                                 |
| auth-username                 | NMS_AUTH_USERNAME                 | sa                                                                                                 | system admin username                                                                                                 |
| cache-timeout                 | NMS_CACHE_TIMEOUT                 | 2S                                                                                                 | http cache timeout for this service in ISO 8601 duration format                                                       |
| certman                       | NMS_CERTMAN                       | true                                                                                               | enable Cordite certman protocol so that nodes can authenticate using a signed TLS cert                                |
| certman-pkix                  | NMS_CERTMAN_PKIX                  | false                                                                                              | enables certman's pkix validation against JDK default truststore                                                      |
| certman-strict-ev             | NMS_CERTMAN_STRICT_EV             | false                                                                                              | enables strict constraint for EV certs only in certman                                                                |
| certman-truststore            | NMS_CERTMAN_TRUSTSTORE            |                                                                                                    | specified a custom truststore instead of the default JRE cacerts                                                      |
| certman-truststore-password   | NMS_CERTMAN_TRUSTSTORE_PASSWORD   |                                                                                                    | truststore password                                                                                                   |
| db                            | NMS_DB                            | .db                                                                                                | database directory for this service                                                                                   |
| doorman                       | NMS_DOORMAN                       | true                                                                                               | enable Corda doorman protocol                                                                                         |
| hostname                      | NMS_HOSTNAME                      | 0.0.0.0                                                                                            | interface to bind the service to                                                                                      |
| mongo-connection-string       | NMS_MONGO_CONNECTION_STRING       | embed                                                                                              | MongoDB connection string. If set to `embed` will start its own mongo instance                                        |
| mongod-database               | NMS_MONGOD_DATABASE               | nms                                                                                                | name for mongo database                                                                                               |
| mongod-location               | NMS_MONGOD_LOCATION               |                                                                                                    | optional location of pre-existing mongod server                                                                       |
| network-map-delay             | NMS_NETWORK_MAP_DELAY             | 1S                                                                                                 | queue time for the network map to update for addition of nodes                                                        |
| param-update-delay            | NMS_PARAM_UPDATE_DELAY            | 10S                                                                                                | schedule duration for a parameter update                                                                              |
| port                          | NMS_PORT                          | 8080                                                                                               | web port                                                                                                              |
| root-ca-name                  | NMS_ROOT_CA_NAME                  | CN="<replace me>", OU=Cordite Foundation Network, O=Cordite Foundation, L=London, ST=London, C=GB  | the name for the root ca. If doorman and certman are turned off this will automatically default to Corda dev root ca  |
| storage-type                  | NMS_STORAGE_TYPE                  | mongo                                                                                              | file | mongo                                                                                                          |
| tls                           | NMS_TLS                           | false                                                                                              | whether TLS is enabled or not                                                                                         |
| tls-cert-path                 | NMS_TLS_CERT_PATH                 |                                                                                                    | path to cert if TLS is turned on                                                                                      |
| tls-key-path                  | NMS_TLS_KEY_PATH                  |                                                                                                    | path to key if TLS turned on                                                                                          |
| web-root                      | NMS_WEB_ROOT                      | /                                                                                                  | for remapping the root url for all requests                                                                           |
| nmp-path                      | NMS_NMP_PATH                      |                                                                                                    | path to network parameters file. sample file can be found in resources folder.                                        |
| allowNodeKeyChange            | NMS_ALLOW_NODE_KEY_CHANGE         | false                                                                                              | to allow registration of a node with same legal name but different legal identity with NMS                            |
| root-ca-file-path             | NMS_ROOT_CA_FILE_PATH             |                                                                                                    | path to root cert file                                                                                                   |

## Node re-registration with same legal name but different identity/key

NMS supports a node to re-register with a different key by setting the property `allow-node-key-change` to `true`. 

`Important Note:` We strongly recommend not to set this property for a production and test environment.

## Doorman protocol

This network map supports the Corda doorman protocol. This facility can be disabled with `doorman` system property or `NMS_DOORMAN` environment variable.

### Retrieving the NetworkMap `network-map-truststore.jks`

If you wish to use the doorman protocol to register a node as per [Corda](https://docs.corda.net/permissioning.html#connecting-to-a-compatibility-zone) you will need the network's `network-map-truststore.jks`.

You can do this using the url `<network-map-url>/network-map/truststore`.

## Certman protocol

This network map provides an alternative means of gaining the required [keystore files](https://docs.corda.net/permissioning.html#installing-the-certificates-on-the-nodes) using any TLS certificate and private key, issued by a formal PKI root CA.

Assuming you have certificate `domain.crt` and its corresponding private key `domain.key`, and assuming the network map is bound to `http://localhost:8080`, the following command line will retrieve the keystore files:

```bash
openssl dgst -sha256 -sign domain.key domain.crt | base64 | cat domain.crt - | curl -k -X POST -d @- http://localhost:8080/certman/api/generate -o keys.zip
```

This essentially signs the certificate with your private key and sends _only_ the certificate and signature to the network-map. 
If the certificate passes validation, the request returns a zip file of the keystores required by the node. 
These should be stored in the `<node-directory>/certificates`.

## Releasing NMS

To release NMS you just need to tag it.  It is then released to docker hub.

## Contributors

This project would not have been possible without the following contributors:

* @dazraf
* Chris Jones
* @nimmaj
* @oj222
* @pinkgrass
* @ajithagiz 
* @opticyclic
* @joeldudleyr3
* @ivanschasny
* @ashu

## License
View [license information](https://gitlab.com/cordite/cordite/blob/master/LICENSE) for the software contained in this image.

As with all Docker images, these likely also contain other software which may be under other licenses (such as Bash, etc from the base distribution, along with any direct or indirect dependencies of the primary software being contained).

As for any pre-built image usage, it is the image user's responsibility to ensure that any use of this image complies with any relevant licenses for all software contained within.