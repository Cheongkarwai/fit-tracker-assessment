# Fit Tracker Assessment

1. Set up environment variables

 - ``DATASOURCE_PASSWORD=mypassword``
 - ``DATASOURCE_USERNAME=myusername``
 - ``DATASOURCE_URL=postgresql://localhost:5432/fit-tracker-assessment``
 - ``FIT_TRACKER_CLIENT_ID=fit-tracker-client``
 - ``FIT_TRACKER_CLIENT_SECRET=W09XX7I1VL01U63RDYW0AI2ZI5401M0KVCDA2CAP``
 - ``FIT_TRACKER_BASE_URL=https://fit-tracker-htmz.onrender.com``
 - ``FIT_TRACKER_API_VERSION=v1``
 - ``REDIS_HOST=localhost``
 - ``REDIS_PORT=6379``
 - ``REDIS_PASSWORD=eYVX7EwVmmxKPCDmwMtyKVge8oLd2t81``
 - ``REDIS_USERNAME=default``

2. Spin up docker-compose

``docker-compose up -d``

3. Run the application

``./gradlew bootRun --args=spring.profiles.active=dev``

4. Access the application

``http://localhost:8080/swagger-ui/index.html``