

.PHONY: apply-manifests
apply-manifests:
	kubectl apply -f infra/processor
	kubectl apply -f infra/prometheus
	kubectl apply -f ./infra/rabbitmq