### Notary Cluster: 

A cluster of nodes acting as notaries to notarise the transactions. In NMS, we have enabled **RAFT based validating and non-validating notary cluster**.  

**How it works**   
Every notary worker node has two legal names. Its own legal name, specified by name, e.g O=Worker 1, C=GB, L=London and the service legal name specified in configuration by notary.serviceLegalName, e.g. O=RAFT, L=Zurich,C=CH. Only the service legal name is included in the network parameters and hence the network map will be advertising only the service identity of the notary cluster. Inside the CorDapp, the notary should be selected based on the notary service identity from the network map cache. Client nodes that request a notarisation by the service name of the notary, will connect to the available worker nodes in a round-robin fashion. The task of a worker node is to verify the notarisation request, the transaction timestamp (if present), and resolve and verify the transaction chain (if the notary service is validating). 
 
Every notary worker’s keystore contains the private key of the worker and the private key of the notary service (with aliases identity-private-key and distributed-notary-private key in the keystore). The service identity is stored in a file distributedService.jks and this jks file should be copied to all notary nodes, placing it in the same directory as the nodekeystore.jks. Then import the distributed-notary-private-key from distributedService.jks into the nodekeystore.jks of all notary nodes.    

As per the current testing, if there are n nodes, then n-1 nodes should be up.  

Todo: To test to find out exactly how many notaries nodes to be up in a cluster. 

**Rules for setting up the service jks**
* Alias name should be `distributed-notary-private-key`
* should have INTERMEDIATE_CA.certificate, ROOT_CA.certificate in the cert chain
* Use the same password for private key as of INTERMEDIATE_CA


**build.gradle Node definition changes**   

For each node acting as notary the following should be specified   
*Node specifications:*
* Notary node name:  is specified as usual. `Examples:  name "O=Worker1,L=Zurich,C=CH",  name "O=NotaryService2,L=Zurich,C=CH"`
* p2pPort: Port on which the node is available for protocol operations over ArtemisMQ.
* rpcSettings: Options for the RPC server exposed by the Node.

*Additional specifications to run the node as Notary:*   

serviceLegalName: The legal name of the cluster. `Example: serviceLegalName: "O=Raft,L=Zurich,C=CH"`   

raft: If part of a Raft cluster then specify raft as described below   

  ```raft: [
        nodeAddress: "localhost:10016",
        clusterAddresses: ["localhost:10008"]
     ]   
  ```
  
nodeAddress:   
The host and port to which to bind the embedded Raft server

clusterAddresses:   
Must list the addresses of all the members in the cluster. At least one of the members must be active and be able to communicate with the cluster leader for the node to join the cluster. If empty, a new cluster will be bootstrapped.  


#### Running the example CorDapp

NMS port used in the example is 8080. 

##### 1. Download the distributedService jks file

Api `http://localhost:8080/network-map/distributed-service/` retrieves the distributedService jks.  

```
mkdir ~/tmp/certificates 
curl -X GET "http://localhost:8080/network-map/distributed-service/" -H "accept: application/octet-stream"  -H "Content-Type: application/json" -d "{  \"x500Name\": \"O=Raft,L=Zurich,C=CH\"}" -o ~/tmp/certificates/distributedService.jks 
```

##### 2. Copy the distributedService jks file to tmp and create nodekeystore.jks

Source and destination keystore password: `cordacadevpass`

```    
keytool --importkeystore --srcalias distributed-notary-private-key --srckeystore ~/tmp/certificates/distributedService.jks --destkeystore ~/tmp/certificates/nodekeystore.jks   
```

##### 3. Check nodekeystore.jks for the private key

keytool -list -v -keystore ~/tmp/certificates/nodekeystore.jks | grep Alias

##### 4. Clone the example

Clone `https://github.com/corda/corda.git` and refer to `samples\notary-demo`

Ref link: `https://github.com/corda/corda/tree/389c91374eeb43b76eca01fb8bfec38ab153f2f4/samples/notary-demo`

##### 5. build.gradle changes

In deployNodesRaft task, change the node names as below:  

 Alice Corp       `>>` AliceCorp    
 Notary Service 0 `>>` NotaryService0   
 Notary Service 1 `>>` NotaryService1    
 Notary Service 2 `>>` NotaryService2    

This step is required as the space in the name breaks some of the following scripts.

Then change `Validating` to `false` if you would like to run non-validating cluster.

##### 6. Clean, build and deployNodes

``` ./gradlew clean samples:notary-demo:deployNodesRaft ```

##### 7. Add the `compatibilityZoneURL` and `devModeOptions.allowCompatibilityZone` to the node.config within each node directory and ensure state is removed from the node directories

``` 
cd samples/notary-demo/
pushd build/nodes/nodesRaft
for N in */; do
      echo 'compatibilityZoneURL="http://localhost:8080"' >> $N/node.conf
      echo 'devModeOptions.allowCompatibilityZone=true' >> $N/node.conf
      pushd $N
      rm -rf network-parameters nodeInfo-* persistence.mv.db certificates additional-node-infos
      popd
done
popd
```

##### 8. Copy the certificates to nodes

```
pushd build/nodes/nodesRaft
for N in */; do
      pushd $N
      cp -r ~/tmp/certificates .
      popd
done
popd
```

##### 9. Register the nodes

```
curl http://localhost:8080/network-map/truststore -o ~/tmp/network-truststore.jks
pushd build/nodes/nodesRaft
for N in */; do
      pushd $N
      java -jar corda.jar --initial-registration --network-root-truststore ~/tmp/network-truststore.jks --network-root-truststore-password trustpass
      popd
done
popd
```

##### 10. Designate the distributed notary
- [ ] login to the NMS API and cache the token

  ```
  TOKEN=`curl -X POST "http://localhost:8080/admin/api/login" -H  "accept: text/plain" -H  "Content-Type: application/json" -d "{  \"user\": \"sa\",  \"password\": \"admin\"}"`
  ```


- [ ] If the notary cluster is non-validating, please execute the below

    ```
    pushd build/nodes/nodesRaft/NotaryService2
    NODEINFO=`ls nodeInfo*`
    curl -X POST "http://localhost:8080/admin/api/notaries/distributed/nonValidating" -H "Authorization: Bearer $TOKEN" -H "accept: text/plain" -H "Content-Type: application/octet-stream" --data-binary @$NODEINFO 
    popd
    ```

- [ ] If the notary cluster is validating, please execute the below  

   ```
      pushd build/nodes/nodesRaft/NotaryService2
      NODEINFO=`ls nodeInfo*`
      curl -X POST "http://localhost:8080/admin/api/notaries/distributed/validating" -H "Authorization: Bearer $TOKEN" -H "accept: text/plain" -H "Content-Type: application/octet-stream" --data-binary @$NODEINFO 
      popd
    ``` 
    If the notary cluster is validating, please perform this step without fail before running the nodes else while running the nodes, notary nodes will shutdown with the error `Configured as validating: true. Advertised as validating: false`
    
    Upload *only one of the notaries's node-info file* from the notary cluster. Uploading multiple notaries will result in duplicate notaries.
    
    API will be  modified in the later release to avoid duplicate entries.  
    
##### 11. Run the nodes
    
    Go to each node and run `java -jar corda.jar`
    
##### 12. Clear Network Cache in case of node startup issues (optional)   
    
   ```
   pushd build/nodes/nodesRaft
   for N in */; do
     pushd $N
     rm -rf network-parameters nodeInfo-* persistence.mv.db persistence.trace.db additional-node-infos
     popd
   done
   popd
   
  ```
        
##### 13. Run the example
   ```
   cd ../.. 
   ./gradlew samples:notary-demo:notarise
    
   ```     
   

    