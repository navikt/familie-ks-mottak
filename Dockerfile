FROM navikt/java:11-appdynamics

ENV APPD_ENABLED=TRUE

COPY ./target/familie-ks-mottak.jar "/app/app.jar"
