# Thau API

Ready-to-use authentication service for your application. With a React connector.

Thau API can be ran inside the docker or as a standalone node service.

Thau API can be configured using ENV variables.

Thau API is documeneted with swagger

Thau API can broadcast events outsidee through different broadcasting channels. Currently Suspported channels: http(s) webhooks

# Run

As a docker image:

`docker run -env-file .env mgrin/thau:latest`

As a standalone service:

```
git clone https://github.com/thau-quester/thau-api.git
cd thau-api
./mvnw clean package
java -jar target/thau-*.jar
```

# Configure

In both cases, the Thau service is configured using env variables. In case on docker, you can pass the `.env` file, otheerwise you have to set the environment variables manually.

### Required values
* `THAU_JWT_RSA_PRIVATE_KEY_BASE64=` - RSA private key, Base64 encoded. *Required* if you want to encrypt your JWT token using RSA (reecommended)
* `THAU_JWT_RSA_PUBLIC_KEY_BASE64=` - RSA public key, Base64 encoded. *Required* if you want to encrypt your JWT token using RSA (reecommended)
* `THAU_JWT_HMAC_SERET=` - HMAC secret key. Required if you want to enccrypt your JWT token using HMAC (NOT recomended)
* `THAU_DATASOURCE_URL=` - jdbc URL to your database. *Required*
* `THAU_DATASOURCE_USERNAME=` - database username. *Required*
* `THAU_DATASOURCE_PASSWORD=` - database password. *Required*

### Optional values
* `THAU_PORT=` - port on which the Thau service will be running. Default - `9000`
* `THAU_ENV=` - environment in which the Thau service is running. Default - `local`
* `THAU_SERVICE_NAME=` - name of the Thau service. Default - `thau`
* `THAU_ENABLE_CORS=` - flag to enable CORS. Default - `false`
* `THAU_JWT_TOKEN_LIFETIME=` - JWT token lifetime, in milliseconds. Default - `864000000` (10 days)
* `THAU_STRATEGIES_PASSWORD_VERIFY_EMAIL=` - flag indicating that acccounts without email verified are not fully functional. Default - `false`
* `THAU_STRATEGIES_GOOGLE_CLIENT_ID=` - Google client ID
* `THAU_STRATEGIES_GOOGLE_CLIENT_SECRET=` - Google client Secret
* `THAU_STRATEGIES_FACEBOOK_CLIENT_ID=` - Facebook client ID
* `THAU_STRATEGIES_FACEBOOK_CLIENT_SECRET=` - Facebook client secret
* `THAU_BROADCAST_HTTP_URL=` - URL to broadcast events through HTTP channel
* `THAU_JPA_DIALECT=` - Hibernate dialect. Default: `org.hibernate.dialect.PostgreSQL92Dialect`
* `THAU_SWAGGER_PATH=` - Path to expose swagger on. Default - `/swagger`
* `THAU_API_PREFIX=` - API prefix path. Default - `/api/v1`

### Minimal configuration example
```
THAU_JWT_HMAC_SERET=secret
THAU_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
THAU_DATASOURCE_USERNAME=admin
THAU_DATASOURCE_PASSWORD=password
THAU_STRATEGIES_PASSWORD_VERIFY_EMAIL=false
```