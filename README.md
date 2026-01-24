# K8S Horizontal Pods Autoscaling (HPA)

**Application**: a Java application "Data Processor" processes data from a queue. The application can process data in parallel in N threads in one application. It is required when all threads are busy to spin a new instance to continue processing messages. To do so, the applciation exposes metrics, among which is a metric for showing number of busy threads. Now, we need to scale properly in K8S the application.

This implementation focuses on horizontally scaling the "Data Processor" component within a Kubernetes cluster. The "Data Processor" is a worker service that consumes messages from RabbitMQ. Because the application uses a fixed-size thread pool for concurrent processing, the primary scaling trigger is thread exhaustion. To simulate real-world load, the "Data Processor" utilizes a "dummy" execution block that holds threads open during message processing. When the ratio of busy-to-available threads crosses a defined threshold, the Horizontal Pod Autoscaler (HPA) should provision additional replicas to maintain throughput and prevent queue buildup.

To make this work, standard HPA metrics (CPU/RAM) aren't enough. If threads are waiting on I/O or network calls, CPU usage might remain low even while the service is "full."
- The Metric Bridge: We should expose a custom metric (e.g., job_processor_threads_used) via an endpoint (like /metrics for Prometheus).
- The Scaling Logic: The HPA controller will query the Custom Metrics API. If out pod has a limit of 10 threads and 8 are busy, the HPA calculates the required replicas.
- Simulation vs. Reality: While the dummy implementation keeps threads busy with "sleep" or "loop" commands, the HPA ensures that as soon as the thread pool nears saturation, the cluster responds before the RabbitMQ "Unacked" count begins to spike.

## Create a cluster

I'm using Kind for local K8S cluster. There are just few command to create a cluster.

```shell
kind delete cluster --name cluster-java-apps
kind create cluster --config ./kind/cluster.yaml --kubeconfig ./kind/kind.kubeconfig
export KUBECONFIG=$PWD/kind/kind.kubeconfig
kubectl cluster-info --context kind-cluster-java-apps --kubeconfig ./kind/kind.kubeconfig
```

## Install Promethus components

I want to install Prometheus components from a Prometheus community Helm repository.
https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack

```shell
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install monitoring prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace

helm upgrade --install prometheus-adapter prometheus-community/prometheus-adapter \
  --namespace monitoring \
  -f ./infra/prometheus-adapter/values.yaml
```

## Build a data processor

Now we need to build a Docker image of our data processor and push it to local Kind's registry - CRI

```shell
docker build . -t processor:0.0.4
kind load docker-image processor:0.0.4 --name cluster-java-apps
```

## The final step

We need to apply manifests to add RabbitMQ service, bring our `processor` as a service, and add Prometheus Agent to scraping metrics and push them to 
a central storage, which in our configuration is in the same cluster

```shell
kubectl apply -f ./infra/rabbitmq
kubectl apply -f ./infra/processor
kubectl apply -f ./infra/prometheus
```

## Create a queue in RabbitMQ

Expose RabbitMQ UI

```shell
kubectl port-forward -n mq service/rabbitmq-service 15672:15672
```

The open a UI on `http://localhost:15672`, user name is `user` and password is `password`.

As a next step we need to create a queue `processor-tasks`.

Then we need to create an exchange `processor-tasks-exchange` with following parameters:
- Routing key: processor.tasks.#
- To (queue): processor-tasks
- Type: topic
- Durable: true

We are ready to send data in our queue and observe how the messages are consumed in our `Data processor`.

## Add messages to queue

Now it is time to send messages to a queue. Do not close a port forward created in a previous step. In a folder `app-processor` there is a file `submit.sh`. Run the file - each run adds one message to the queue. As soon as threads in a pod are busy, then a new pod is added to the cluster and it starts to consume messages from a queue.