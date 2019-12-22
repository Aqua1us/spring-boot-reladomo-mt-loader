# Spring Boot Reladomo MT Loader
This is a sample application to try out Reladomo's Multi-Threaded matcher Loader (MT Loader).

# Run the project
## Start a MySQL server instance in Docker container
```bash
$ docker-compose up -d
```

## Connect to MySQL
```bash
$ docker-compose exec mysql bash -c 'mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE}'
```