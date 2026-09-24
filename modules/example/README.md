# ZIO Elasticsearch Example Application

This application represents an example of usage `zio-elasticsearch` library for **Elasticsearch 7.x**.

### Running

- Run the Elasticsearch 7.x service (one can be found as part of the `docker-compose.yml` file in the root of this
  repository)
- Start the application by running the following command:
  ```shell
    ./sbt example/run
    ```
- Check whether the application is running [here](http://localhost:9000/health)
- Explore endpoints using Postman collection (`zio-elasticsearch-example.postman_collection.json`)

### Description

On the application startup - a **"repositories"** index will be deleted, and immediately re-created with the mapping
definition given in the `resources/mapping.json` file.

After successfully starting the application, you can test the exposed ZIO Elasticsearch library's API through exposed HTTP
endpoints.
