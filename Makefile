# khermes-env-demo makefile

# Environment Variables

.PHONY: up

prep :
	mkdir -p \
		data/whisper \
		data/elasticsearch \
		data/grafana \
		log/graphite \
		log/graphite/webapp \
		log/elasticsearch

up : prep
	docker-compose -f docker/landoop-demo-compose.yml up

down :
	docker-compose down