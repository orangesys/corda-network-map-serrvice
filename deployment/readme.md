# Deployment

# CI/CD
  + This repo is integrated to Azure AKS. See CI/CD->Kubernetes for details of which cluster.
  + The cluster has a runner deployed and all CI jobs in gitlab-ci.yaml spawn pods in the cluster to run.
  + network-map-service environment can be built using `./deployment/kube_deploy.sh`
  + DNS is provided by CloudFlare and configured using [external-dns](https://github.com/kubernetes-incubator/external-dns)
  + external-dns runs in the kube-system namespace and is deployed using `./deployment/external-dns.yaml'
  + More details on CI/CD and recreating this integration can be found in `./deployment/readme.md`
  + Persistent storge mapped to NMS_DB_DIR under storage account `corditeedge8` as an Azure file share
  + Release CI job uses deployment rollout strategy to release newer version of image
  + Pods can scale horizontally by changing `replicas: 1` in `./deployment/deployment.yaml`


# NMS deployment
```
kubectl delete -f deployment.yaml
kubectl create -f deployment.yaml
```

## TLS cert create
```
kubectl create secret generic cordite-tls-cert --from-file=cordite-tls.crt=cordite-biz.crt --from-file=cordite-tls.key=cordite-biz.key
```

### Add Kube Runner to cluster
https://docs.gitlab.com/ee/install/kubernetes/gitlab_runner_chart.html  
```
helm repo add gitlab https://charts.gitlab.io                                                                   
helm init
helm install --namespace network-map-service --name gitlab-runner-nms -f gitlab-runner-config.yaml gitlab/gitlab-runner
```
to remove runner `helm del --purge gitlab-runner-nms`



You can also use the `kube_deploy.sh`. 
Make sure you have set all the environment variables correctly.
```
IMAGE_TAG=${CI_PIPELINE_ID:-latest} # image tag you want deployed
KUBE_NAMESPACE=${KUBE_NAMESPACE:-default} # Kube namespace you want to deploy to
CI_ENVIRONMENT_SLUG=${CI_ENVIRONMENT_SLUG:-network-map-dev} # name of your environment
CI_REGISTRY=registry.gitlab.com
NMS_REG_USER=<gitlab deploy user> # see gitlab CI secret variables
NMS_REG_TOKEN=<gitlab deploy token> # see gitlab CI secret variables
```

## To do
- [ ] kube_deploy.sh is not bullet proof and in need of TLC

## How do I get Logs, Kube UI, public IP
```
kubectl -n currency-pay-dgl logs deployment/nwm-dgl-dev
az aks browse --resource-group cordite-edge8 --name cordite-edge8
kubectl -n currency-pay-dgl get services --watch
kubectl exec -n currency-pay-dgl -it deployment/nwm-dgl-dev -- /bin/bash
```

## Gitlab/Kubernetes integration
Use your BB gitlab project name for the namespace
```
$ kubectl create namespace network-map-service
$ kubectl -n network-map-service create serviceaccount network-map-service
$ kubectl -n network-map-service create rolebinding network-map-service --clusterrole=admin --serviceaccount=network-map-service:network-map-service
$ kubectl -n network-map-service get sa/network-map-service -o yaml
$ kubectl -n network-map-service get secret network-map-service-token-<postfix> -o yaml
$ kubectl cluster-info
```
From the secret you need `ca.crt` and `token`. Both are coded in base64. Decode using `base64 -D`. for example
```
KUBE_TOKEN=$(kubectl -n currency-pay-dgl get secret currency-pay-dgl-token-2l6wn -o jsonpath={.data.token} | base64 -D)
```
From the cluster-info you need the URL shown as `Kubernetes master is running at`

### Add Kube config to repo
Go to CI/CD -> Kubernetes -> Add custom cluster. Complete the following fields and save.
   + Kubernetes cluster name : aks-region-a-aks
   + API URL : `Kube Master API URL from previous step`
   + CA Certficate : `decoded ca.crt from previous step`
   + Token : `decoded token from previous step`
   + Project namespace : `name space from previous step`  

Click to install Helm Tiller, Ingress (not required), Prometheus (metrics), Gitlab Runner (for CI) on your cluster

## Things they don't tell you
  + $KUBE_CONFIG is a CI variable which deals with all security context on Kube runner
  + Adding label app=<environment> will make environments and metrics work in gitlab

### External DNS (cluster wide resource)
We are using CloudFlare and Kube ExternalDNS - https://github.com/kubernetes-incubator/external-dns
To find out more see - https://github.com/kubernetes-incubator/external-dns/blob/master/docs/tutorials/cloudflare.md  
Only need one of these per domain in the cluster kube-system namespace 
Follow the logs with `kubectl -n kube-system logs deployment/external-dns`


### TLS certificates (cluster wide resource)
We are using CloudFlare and Kube cert-manager - https://cert-manager.readthedocs.io/
Only need one of these per domain in the cluster kube-system namespace 
Follow the logs on the cert-manager pod in namespace=kube-system
```
kubectl get certificates -n currency-pay-dgl
kubectl describe certificate nwm-dgl-edge-cordite-biz -n currency-pay-dgl
```