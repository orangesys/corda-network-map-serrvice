deploy: 
	kubectl apply -f ./deployment/k8s/

delete:
	kubectl delete -f ./deployment/k8s/