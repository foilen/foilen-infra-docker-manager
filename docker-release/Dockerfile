FROM openjdk:11-stretch

RUN export TERM=dumb ; \
  apt-get update && apt-get install -y \
    curl ca-certificates \
  && apt-get clean && rm -rf /var/lib/apt/lists/*

# Infra apps to share
RUN export TERM=dumb ; \
	wget https://deploy.foilen.com/foilen-haproxy/foilen-haproxy_1.7.14-1_amd64.deb \
	&& dpkg -i foilen-haproxy_1.7.14-1_amd64.deb \
	&& rm foilen-haproxy_1.7.14-1_amd64.deb \
	&& wget https://deploy.foilen.com/services-execution/services-execution_1.0.1_amd64.deb \
	&& dpkg -i services-execution_1.0.1_amd64.deb \
	&& rm services-execution_1.0.1_amd64.deb

COPY app/ /app

WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/foilen-infra-docker-manager.jar"]
