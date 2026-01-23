```shell
kind delete cluster --name cluster-java-apps
kind create cluster --config ./kind/cluster.yaml --kubeconfig ./kind/kind.kubeconfig
export KUBECONFIG=./kind/kind.kubeconfig
kubectl cluster-info --context kind-cluster-java-apps --kubeconfig ./kind/kind.kubeconfig
```


```shell
kubebuilder init --domain fintech --repo github.com/alex538/jvm-observation
kubebuilder create api --group diagnostics --version v1 --kind JvmObservation
```

```shell
kind load docker-image image_name:latest --name image_name_in_kind
```



## Scheduler

There is a java application which processes an incoming request from a message queue. There are not really many requests.
One pod can handle N requests. Maximum can be P pods, total number of simultaneously processing requests is N*P, default is 1 pod.
When a pod's number of processing requests exceeds N, then a new pod spins up and starts to accept and process requests.
When a pod stops processing requests and idles then operator considers to stop the pod.

Pod need to expose number of processing threads.
Scheduler need to observe pods and decide to spin up/down a pod.


JavaProcessorApplicationCR:
- max number of pods
- max number of processing requests in one pod

