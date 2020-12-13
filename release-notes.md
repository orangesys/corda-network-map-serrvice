# Cordite NMS 0.5.0

## New Features:

* [96](https://gitlab.com/cordite/network-map-service/issues/96): Notary clusters support
    * Enabled RAFT based validating and non-validating notary cluster.  
    [Notary Cluster Readme](https://gitlab.com/cordite/network-map-service/blob/release-notes/NotaryCluster.md)
* [110](https://gitlab.com/cordite/network-map-service/issues/110): Pass network parameters via env variable
    * Enabled env variable to specify network parameters while starting NMS
* [122](https://gitlab.com/cordite/network-map-service/issues/122): Implement ack-parameters  
    * ack-parameters api was not fully implemented in the previous version. In this version, when the node calls ack-parameters api with the latest accepted network parameters, nms stores it against the node-info.
* [112](https://gitlab.com/cordite/network-map-service/issues/112): Alter the network parameters at runtime
    * Added an api to modify the current network parameters at runtime  
    URL: http://<host>:<port>/admin/api/replaceAllNetworkParameters  
    Method: POST
* [123](https://gitlab.com/cordite/network-map-service/issues/123): Delete all nodes
    * Added an api to delete all nodes from the network map storage  
    URL: http://<host>:<port>/admin/api/nodes/  
    Method: DELETE

## Fixed Bugs:

* [117](https://gitlab.com/cordite/network-map-service/issues/117): NMS test certificate expired
    * Created a self signed cert with validity of 99999 days and updated the TEST_CERT and TEST_PRIV_KEY
* [119](https://gitlab.com/cordite/network-map-service/issues/119): Delete node in admin page
    * Fixed delete node button's click event
* [125](https://gitlab.com/cordite/network-map-service/issues/125): Retry retrieving updated network parameters 
    * Network Map parameters update happens after a delay period. In order to test whether the update has happened, we need to poll the current network parameters api. Fixed the bug that affected the method that helps to retry calling the api until we get the updated nms parameters
* [120](https://gitlab.com/cordite/network-map-service/issues/120): Added Index.html to app folder
    * index.html was only in public folder, so if anyone deletes public folder and runs brunch build to create one, it will be lost.
* [118](https://gitlab.com/cordite/network-map-service/issues/118): Added package-lock.json
    * package-lock.json was not in the repository and it is added now
* [114](https://gitlab.com/cordite/network-map-service/issues/114): Error message
    * Updated error message to 'failed to add a non-validating notary' from 'failed to add a validating notary'
    
    
## Documentation: 

* [115](https://gitlab.com/cordite/network-map-service/issues/115): Updated the FAQ tutorial for setting up a network  
* [106](https://gitlab.com/cordite/network-map-service/issues/106): Added documentation for updating the contracts whitelist

    
      
      
