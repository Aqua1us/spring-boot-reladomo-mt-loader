# Spring Boot Reladomo MT Loader
[![Build Status](https://travis-ci.com/amtkxa/spring-boot-reladomo-mt-loader.svg?branch=master)](https://travis-ci.com/amtkxa/spring-boot-reladomo-mt-loader)

This is a sample application  that implemented Reladomo's Multi-Threaded matcher Loader (MT Loader).

## Run the project
### Start a MySQL server instance in Docker container
```bash
$ docker-compose up -d
```

### Connect to MySQL
```bash
$ docker-compose exec mysql bash -c 'mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE}'
```

### Build and run the application
```bash
# install dependencies and build application
$ gradle build

# start the API server at localhost:8080
$ java -jar ./build/libs/spring-boot-reladomo-mt-loader-0.0.1-SNAPSHOT.jar 
```