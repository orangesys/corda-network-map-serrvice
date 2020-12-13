# Network Map Service FAQ

## Contents

1. [Show me how to setup a simple network](#1-show-me-how-to-set-up-a-simple-network)
2. [How do I set up TLS](#2-how-do-i-setup-tls)
3. [How do I add a node to a network run using Java?](#3-how-do-i-add-a-node-to-a-network-run-using-java)
4. [How do I join the Cordite Test network?](#4-how-do-i-join-the-cordite-test-network)
5. [How do I admin the embedded database](#5-how-do-i-admin-the-embedded-database)
6. [How do I add contract to the whitelist](#6-how-do-i-add-contract-to-the-whitelist)
7. [How do I pass JKS files for NMS](#7-how-do-i-pass-jks-files-for-nms)
8. [Delete all nodes](#8-delete-all-nodes)
9. [Delete validating notary](#9-delete-validating-notary)
10. [Delete non-validating notary](#10-delete-non-validating-notary)
11. [How to setup a notary cluster](notary-cluster.md)
12. [Know more about network parameters](network-parameters.md)

## Questions

### 1. Show me how to set up a simple network

Steps:

* 1.1 Start the NMS
* 1.2 Prepare the cordapp project
* 1.3 Register the nodes
* 1.4 Start the notary node
* 1.5 Designate the notary node
* 1.6 Stop the notary node
* 1.7 Delete the network-parameters file on the notary node
* 1.8 Start the notary node and other nodes
* 1.9 Running the example CorDapp

~~Video of the following section being demonstrated on a laptop available [here](https://www.youtube.com/watch?v=NczNdVxEZyM).~~


#### 1.1 Start the NMS ...

##### ... the Docker way
- `docker run -p 8080:8080 -e NMS_STORAGE_TYPE=file cordite/network-map` 
- check it's started using a browser http://localhost:8080

##### ... the Java way

Alternatively you can run it using traditional Java development tools.

Install dependencies: 
* JDK 8u181
* NodeJS 11

Then build the project:
```bash 
mvn clean install -DskipTests
```

Execute the networkmap:

```bash
cd target
java -jar network-map-service.jar
```

#### 1.2. Prepare the Cordapp project
- [ ] checkout the samples repo and go to cordapp-example(or any other cordapp project)
    
    ```bash
    git clone git@github.com:corda/samples.git
    ```

- [ ] ensure that your cordapp X509 names have the following fields: 
  * `L` - Location
  * `C` - Country
  * `O` - Organisation
  * `OU` - Organisation Unit
- [ ] build the nodes:
  
  ```bash
  ./gradlew clean deployNodes
  ```
  
  or to build quickly
  
  ```bash
  ./gradlew clean workflow-kotlin:deployNodes
  ```

  
- [ ] add the `compatibilityZoneURL` and `devModeOptions.allowCompatibilityZone` to the node.config within each node directory and ensure that all state is removed from the node directories

  ```bash
  pushd build/nodes
  for N in */; do
        echo 'compatibilityZoneURL="http://localhost:8080"' >> $N/node.conf
        echo 'devModeOptions.allowCompatibilityZone=true' >> $N/node.conf
        pushd $N
        rm -rf network-parameters nodeInfo-* persistence.mv.db certificates additional-node-infos
        popd
  done
  popd
  ```
  
#### 1.3. Register the nodes
  - [ ] download the network truststore

      ```bash 
      curl http://localhost:8080/network-map/truststore -o ~/tmp/network-truststore.jks
      ```
  - [ ] for each node run initial registration

    ```bash
    pushd build/nodes
    for N in */; do
          pushd $N
          java -jar corda.jar --initial-registration --network-root-truststore ~/tmp/network-truststore.jks --network-root-truststore-password trustpass
          popd
    done
    popd
    ```
#### 1.4. Start the notary node

- [ ] Navigate to notary node directory and excecute

  ```bash
  java -jar corda.jar
  ```
  
  - [ ] check that the notary node has been registered with the NMS [http://localhost:8080](http://localhost:8080)

#### 1.5 Designate the notary
- [ ] login to the NMS API and cache the token

  ```bash
  TOKEN=`curl -X POST "http://localhost:8080//admin/api/login" -H  "accept: text/plain" -H  "Content-Type: application/json" -d "{  \"user\": \"sa\",  \"password\": \"admin\"}"`
  ```

- [ ] Upload the notary

    ```bash
    pushd build/nodes/Notary
    NODEINFO=`ls nodeInfo*`
    curl -X POST -H "Authorization: Bearer $TOKEN" -H "accept: text/plain" -H "Content-Type: application/octet-stream" --data-binary @$NODEINFO http://localhost:8080//admin/api/notaries/validating
    popd
    ```

#### 1.6 Stop the notary node
- [ ] In the notary node shell, execute `bye`

#### 1.7 Delete the network-parameters file on the notary node
- [ ] In the notary node directory, remove the `network-parameters` file

#### 1.8 Start the notary node and other nodes
- [ ] check that all the nodes have been registered with the NMS [http://localhost:8080](http://localhost:8080)

#### 1.9 Running the example CorDapp
- [ ] This CorDapp is documented here. [https://docs.corda.net/tutorial-cordapp.html](https://docs.corda.net/tutorial-cordapp.html)
  

### 2. How do I set up TLS?

Corda places certain requirements for connecting to any network map that's been secure with TLS.
Notably it requires formal certificates from any of the existing well-know root certificate authorities, recognised by the JRE.

Therefore to enable TLS, you will need:

* A DV certificate from any of the major CAs. You can get a free one from [Let's Encrypt](https://letsencrypt.org/)
* You need to be running your NMS on a server with the hostname referenced by the certificate. It's not recommended to try this on a dev laptop/workstation.

Then you will need to configure the NMS to use your certificate and private key. 
The following are instructions for doing this using both Docker as well as the java command line.

#### Using TLS certificates with the Docker NMS image

Assuming you have a directory on your host called with path `/opt/my-certs`, containing your certificate `tls.crt` and private key `tls.key`.

```
docker run -p 8080:8080 \
    -e NMS_TLS=true \
    -e NMS_TLS_CERT_PATH=/opt/certs/tls.crt \
    -e NMS_TLS_KEY_PATH=/opt/certs/tls.key \
    -v /opt/my-certs:/opt/certs \
    cordite/network-map
```

#### Using TLS certificates when running the NMS jar using Java

Again, assuming the same certificate and key paths, you can pass these in using 
Java system properties e.g.

```
java \
-Dtls=true \
-Dtls-cert-path=/opt/my-certs/tls.crt \
-Dtls-key-path=/opt/my-certs/tls.key \
-jar target/network-map-service.jar
```

### 3. How do I add a node to a network run using Java?

  + Start the network map service with TLS disabled (`$ java -Dtls=false -jar target/network-map-service.jar`)
    + If you don't disable TLS and you don't have a valid TLS certificate for the network map service, nodes will not 
      be able to join the network
  + Create a Corda node
  + Clean out the node if required by deleting the contents of the `certificates` and `additional-node-infos` folders, and the `persistence.mv.db` and `network-parameters` files 
  + Point the node to your network map service by adding the following line to the node's `node.conf` file: 
    `compatibilityZoneURL="http://localhost:8080"`
  + Download the network root truststore from `http://localhost:8080/network-map/truststore` and place it in the node's 
    folder under `certificates/`
  + Register the node with the network map service using `java -jar corda.jar --initial-registration --network-root-truststore-password trustpass`
  + Start the node using `java -jar corda.jar`
  + Visit the network map UI at `https://localhost:8080` to see the node

### 4. How do I join the Cordite Test network

See this awesome [video from André van der Heijden](https://www.youtube.com/watch?v=lfk-QSiv3xc).

### 5. How do I admin the embedded database

> PLEASE READ THIS FIRST:
> The embedded database is _ONLY_ for throwaway testing where the data is of no value. Please do NOT use in any other scenario.
> Also please note that the support for the use of MongoDB may change in the future, with increasing demand for SQL support.

1. In your container log you should see a line like this:
```
2019-06-01 04:04:52,842 INFO  i.c.n.storage.mongo.EmbeddedMongo - mongo database started on mongodb://mongo:mongo@localhost:41687 mounted on /opt/cordite/db/mongo
```

Note the port number. It will vary with each container. In this case: `41687`. 


2. Shell into your container

```
docker exec -it <container-id> /bin/bash
```

3. Connect and authenticate to your local mongo database

```
~/.embedmongo/extracted/Linux-B64--4.0.4/extractmongo admin -u mongo -p mongo --port <mongod-port-from-step-1>
```

4. In the mongo shell, select the `nms` db

```
show databases
use nms
show collections
# etc
```

### 6. How do I add contract to the whitelist

> PLEASE READ THIS FIRST:
> Corda v4 provides Signature constraint mechanism which provides better flexibility to add new contracts.
> All nodes in the network using older network-parameters file will not function and shutdown. Delete the network parameter file on each of those and restart the node to automatically download and sync the new version of network-parameters.


Cordite network map provides whitelisting api to append or replace the contract. Use POST request to replace all the existing whitelist and PUT request to append the new contract to existing whitelisted contracts. The following endpoint adds or replaces the contract:
```
/admin/api/whitelist
```

1. Get the jwt token using Login API by any of the below two methods:
+ Use the Swagger endpoint
```
http://localhost:8080/swagger/#/admin/post_admin_api_login
```
+ Type the following CURL command in terminal (assuming default credentials)
```
curl -X POST -d "{\"user\": \"sa\",\"password\": \"admin\"}" http://localhost:8080//admin/api/login
```

2. Get hash of all the contracts using one of the following methods:
+ Use the `whitelist.txt` file generated while using `gradle clean deployNodes` command in local 
+ Manually compute hash of all the contract jars
   + For Windows use the below command in cmd
   ```
  certUtil -hashfile <fileName.jar> SHA256
  ``` 
  + For Ubuntu/ Linux use the below command in terminal
  ```
  sha256sum <fileName.jar>
  ```
  + For Mac, use the below command in terminal
  ```
  shasum -a 256 <fileName.jar>
  ```

3. For each of the contract jars, use the following curl request to add contracts to whitelist
- [ ] To append the contract to existing whitelist
```
curl -X PUT -H "Authorization: Bearer <jwt token received in Step 1>" -H "accept: text/plain" -d "<fully qualified contract class name>:<contract hash>" http://localhost:8080//admin/api/whitelist
```

- [ ] To remove all existing whitelisted contract and add new one
```
curl -X POST -H "Authorization: Bearer <jwt token received in Step 1>" -H "accept: text/plain" -d "<fully qualified contract class name>:<contract hash>" http://localhost:8080//admin/api/whitelist
```

4. For each node in the network, delete the existing network-parameters file and restart the node

### 7. How do I pass JKS files for NMS

Notes

* You need to do all of these steps in order
* if you do these, you will generate a cert valid for 3 months
* you can add `-validity 360` to generate one for 360 days
* there is an example script [here](src/test/resources/certificates/root/generateNewRootKeyStore.sh)

#### Step 1: To generate root keystore if not already created by other means(Optional)     
 
- [ ] Create ca key pair and ca pem file
    ```bash
    keytool -genkeypair -keyalg EC -keysize 256 -alias ca -dname "CN=Root CA, OU=Cordite Foundation Network, O=Cordite Foundation, L=London, ST=London, C=GB" -ext bc:ca:true,pathlen:1 -ext bc:c -ext eku=serverAuth,clientAuth,anyExtendedKeyUsage -ext ku=digitalSignature,keyCertSign,cRLSign -keystore ca.jks -storepass changeme -keypass changeme
      
    keytool -exportcert -rfc -alias ca -keystore ca.jks -storepass changeme -keypass changeme > ca.pem
    ```  
    
- [ ] Create key pairs with cert and key alias  
    ```bash
    keytool -genkeypair -keyalg EC -keysize 256 -alias cert -dname "CN=Root CA, OU=Cordite Foundation Network, O=Cordite Foundation, L=London, ST=London, C=GB" -ext bc:ca:true,pathlen:1 -ext bc:c -ext eku=serverAuth,clientAuth,anyExtendedKeyUsage -ext ku=digitalSignature,keyCertSign,cRLSign -keystore root.jks -storepass changeme -keypass changeme
     
    keytool -genkeypair -keyalg EC -keysize 256 -alias key -dname "CN=Root CA, OU=Cordite Foundation Network, O=Cordite Foundation, L=London, ST=London, C=GB" -ext bc:ca:true,pathlen:1 -ext bc:c -ext eku=serverAuth,clientAuth,anyExtendedKeyUsage -ext ku=digitalSignature,keyCertSign,cRLSign -keystore root.jks -storepass changeme -keypass changeme
    ```
    
- [ ] Create certificate signing request and import into keystore root.jks  
    ```bash
    keytool -certreq -alias cert -keystore root.jks -storepass changeme -keypass changeme | keytool -gencert -ext eku=serverAuth,clientAuth,anyExtendedKeyUsage -ext bc:ca:true,pathlen:1 -ext bc:c -ext ku=digitalSignature,keyCertSign,cRLSign -rfc -keystore ca.jks -alias ca -storepass changeme -keypass changeme > cert.pem
      
    keytool -certreq -alias key -keystore root.jks -storepass changeme -keypass changeme | keytool -gencert -ext eku=serverAuth,clientAuth,anyExtendedKeyUsage -ext bc:ca:true,pathlen:1 -ext bc:c -ext ku=digitalSignature,keyCertSign,cRLSign -rfc -keystore ca.jks -alias ca -storepass changeme -keypass changeme > key.pem
        
    keytool -importcert -noprompt -file cert.pem -alias cert -keystore root.jks -storepass changeme -keypass changeme
    
    keytool -importcert -noprompt -file key.pem -alias key -keystore root.jks -storepass changeme -keypass changeme
    ```
     `Note:   
     importcert will show an error "Failed to establish chain from reply" as the issuer ca certificate is not in the keychain and this can be ignored`

- [ ] If the generated root.jks is passed to NMS, we will get invalid signature so to resolve that issue, follow the below steps 
    ```bash
    mv root.jks old.jks
      
    keytool -importkeystore -srcstorepass changeme -srckeystore old.jks -deststorepass changeme -destkeystore old.p12 -deststoretype pkcs12 
    
    openssl pkcs12 -in old.p12 -out pemfile.pem -nodes -passin pass:changeme -passout pass:changeme
    
    openssl pkcs12 -export -in pemfile.pem -name cert -out cert.p12 -passin pass:changeme -passout pass:changeme
    
    openssl pkcs12 -export -in pemfile.pem -name key -out key.p12 -passin pass:changeme -passout pass:changeme
    
    keytool -importkeystore -srcstorepass changeme -srckeystore cert.p12 -deststorepass changeme -destkeystore root.jks -srcstoretype pkcs12 
    
    keytool -importkeystore -srcstorepass changeme -srckeystore key.p12 -deststorepass changeme -destkeystore root.jks -srcstoretype pkcs12 

    ```
- [ ] Check keystore 
    ```bash
    keytool -list -v -keystore root.jks -storepass changeme
    ```
#### Step 2: Pass root keystore and start NMS   
    
- [ ] Run NMS jar by passing path to root jks file using `root-ca-file-path` property  

    ```bash
    java -jar -Droot-ca-file-path="<path to the file>" network-map-service.jar
    ```
        
- [ ] Run NMS via docker

    ```bash
    docker run -v "<path to the file>":/opt/cordite/certs -e NMS_ROOT_CA_FILE_PATH="<path to the file>" -p 8080:8080 cordite/network-map
    ```   
    
    Sample:
    ```bash
    docker run -v ~/tmp/:/opt/cordite/certs/ -e NMS_ROOT_CA_FILE_PATH=/opt/cordite/certs/root.jks -p 8080:8080 cordite/network-map
    ```
    
### 8. Delete all nodes

- [ ] login to the NMS API and cache the token

  ```bash
  TOKEN=`curl -X POST "http://localhost:8080/admin/api/login" -H  "accept: text/plain" -H  "Content-Type: application/json" -d "{  \"user\": \"sa\",  \"password\": \"admin\"}"`
  ```

- [ ] delete all nodes
  ```bash
  curl -X DELETE -H "Authorization: Bearer $TOKEN" http://localhost:8080/admin/api/nodes/
  ```  

- [ ] delete node via admin page   

  Login to http://localhost:8080/ and delete the node by clicking on the trash icon.
  
  
### 9. Delete validating notary  
  `nodeKey` used as data in the below api curl can be copied from the nodeinfo file name.   
  Sample nodeinfo file name is `nodeInfo-777DA369F066FE34BEDE3E6334A1006A4026A02DD76AFA798204BD015C9965DE`. nodeKey is the hash present in the file name.

- [ ] login to the NMS API and cache the token

  ```bash
  TOKEN=`curl -X POST "http://localhost:8080/admin/api/login" -H  "accept: text/plain" -H  "Content-Type: application/json" -d "{  \"user\": \"sa\",  \"password\": \"admin\"}"`
  ```
  
- [ ] delete validating notary
    
  ```bash
  curl -X DELETE -H "Authorization: Bearer $TOKEN" http://localhost:8080/admin/api/notaries/validating -d 'nodeKey'

  ```    
  
### 10. Delete non-validating notary
    
- [ ] login to the NMS API and cache the token

  ```bash
  TOKEN=`curl -X POST "http://localhost:8080/admin/api/login" -H  "accept: text/plain" -H  "Content-Type: application/json" -d "{  \"user\": \"sa\",  \"password\": \"admin\"}"`
  ```

- [ ] delete non-validating notary
  ```bash
  curl -X DELETE -H "Authorization: Bearer $TOKEN" http://localhost:8080/admin/api/notaries/nonValidating -d 'nodeKey'
  ```

