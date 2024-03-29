FROM postgres:14.5-alpine

COPY ./init.sql /docker-entrypoint-initdb.d/init.sql

RUN chmod +x /docker-entrypoint-initdb.d/init.sql
