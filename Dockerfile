FROM navikt/java:11-appdynamics

ENV APPD_ENABLED=true
ENV APP_NAME=familie-ks-mottak

COPY ./target/familie-ks-mottak.jar "app.jar"
