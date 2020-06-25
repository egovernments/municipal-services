FROM egovio/alpine-maven-builder-jdk-8:11-master-6036091e AS build
FROM egovio/flyway:4.1.2

COPY ./migration/ddl /flyway/sql

COPY migrate.sh /usr/bin/migrate.sh

RUN chmod +x /usr/bin/migrate.sh

CMD ["/usr/bin/migrate.sh"]
