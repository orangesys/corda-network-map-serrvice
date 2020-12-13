### Network Parameters: 

Network parameters are a set of values that every node participating in the zone needs to agree on and use to correctly interoperate with each other.
These parameters are advertised by the network map service. If the node is using the HTTP network map service then on first startup it will download the signed network parameters, cache it in a network-parameters file and apply them on the node.

If the network-parameters file is changed and no longer matches what the network map service is advertising then the node will automatically shutdown. Resolution to this is to delete the stale file and restart the node so that the parameters can be downloaded again.

## Default network parameters:

Ref [here](https://gitlab.com/cordite/network-map-service/blob/master/src/main/kotlin/io/cordite/networkmap/serialisation/NetworkParametersMixin.kt) for the default network parameters used by NMS.

## Pass network parameters:

Network parameters can be specified while starting NMS using java property nmp_path or env property NMP_PATH. The value of this property is the path to the network parameters file and the file should be in JSON format. This will override the default network parameters.

NMS does not support specifying notaries as part of the network parameters file in this release. Support will be added in the future releases.

## Update network parameters:

There are many reasons that can modify the network parameters: adding a notary, adding a contract to whitelistedContractImplementations etc.   
Updating of the parameters is done in two phases: 
1. Advertise the proposed network parameter update to the entire network. 
2. Switching the network onto the new parameters - also known as a flag day.

If the only changes between the current and new parameters are for auto-acceptable parameters then, unless configured otherwise, the new parameters will be accepted without user input.  
Following are the auto-acceptable parameters:  
    1. modifiedTime  
    2. epoch  
    3. whitelistedContractImplementations  
    4. packageOwnership  
    
If the auto-acceptance behaviour is turned off via the configuration or the network parameters change involves parameters that are not auto-acceptable then manual approval is required.  

## Replace network parameters:

`replaceAllNetworkParameters` protected api allows to replace all the network parameters.

URL: http://host:port/admin/api/replaceAllNetworkParameters

Sample JSON input: 
``` 
{
  "minimumPlatformVersion": 4,
  "maxMessageSize": 10485760,
  "maxTransactionSize": 2147483647,
  "epoch": 1,
  "whitelistedContractImplementations": {},
  "eventHorizon": 2592000,
  "packageOwnership": {}
}
```

If any one of these parameters are not included in the input json, NMS uses the default value

