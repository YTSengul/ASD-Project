# Chatter Directory Server: Java Spring
This is the repository for the chatter directory server.

## Prerequisites
- An installation of MongoDB is needed for this server.

## Useful links
Cloud repo: https://bitbucket.org/DennisHak/chatter_directory_server_submarine_cloud/src/  
Sonar: https://sonarcloud.io/dashboard?branch=development&id=Dennis-Hakvoort_chatter_directory_server_submarine_cloud

## Endpoints

### Create chatter
```
url:            /chatter/create
method:         POST
status code:    201 (Created)
```

Request body need to be conform the ChatterRegistrationDTO model. Example in JSON:
```
{
    "alias": "TEST_ALIAS",
    "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAil95F4w0F0xcn8teY/LqT98Eo0/U7y0u6qMyYXqnXoQ/BA22rUU8GVF4WOHgn4VE1LwYPJdv6TIBmd1IbTTlzF9+NMPHs1njxrcY4NpWTsQc5diyU3ulh/bD0EY5buHluKCMN9RImHTaYaZKdK5T0lT3UJ5o6EKBBk2JDXx8IjkQ3A1P+m6lzflLc8ohSaDr1An/QpHxjND965xRwFl7ZURGNGiWMJmrHmGXs3uOxcrDwUAgWVIY1xu9U9JPiZCNx2q1oICOpBkhoOmJfT77h5f9cS0kSQUprH6hPV+FLPKaEDi5IIqEQ2fB3Si88T2pO/QjIbvqsQVdUe7GQ+QECQIDAQAB",
    "username": "TEST_USERNAME",
    "password": "TEST_PASSWORD",
    "ipAddress": "TEST_IP"
}
```
Of note here is that the publicKey should be a base64 encoded string representing the bytes of the public key

Common exceptions:
```
status 400:     Bad Request
message:        "Fill in all fields"

status 400:     Bad Request
message:        "Alias or username already exist"
```

### Update ip
```
url:            /chatter/login
method:         PUT
status code:    200 (Ok)
```

Request body need to be conform the ChatterLoginDTO model. This is filled with username, password and ip address. 
```
{
    "username": "TEST_USERNAME",
    "password": "TEST_PASSWORD",
    "ipAddress": "TEST_IP"
}
```

Common exceptions:
```
status 403:     Forbidden
message:        "Username or password is incorrect"

status 400:     Bad Request
message:        "The given IPv4 address is invalid"

status 400:     Bad Request
message:        "Not all fields are filled in. Expected:
                 {
                    "username": (String),
                    "password": (String),
                    "ipAddress": (String)
                 }"

status 500:     Internal Server Error
message:        "A method was unknown"
```

### Get ip
```
url:            /chatter/get-ip/{alias}
method:         GET
status code:    200 (Ok)
```
Response body send back ChatterAddressDTO. This is filled with ipAddress.
```
{
    "ipAddress": (String)
}
```
Common exceptions:
```
status 400:     Bad Request
message:        "Could not find user by alias"
```
