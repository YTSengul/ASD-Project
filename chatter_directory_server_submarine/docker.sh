docker run -p 27017:27017 --name mongo-db -d mongo

mvn package && docker build . -t dennishk/asd-chatter-directory-server-submarine

docker rm -f chattercontainer

docker run -p 8080:8080 -t --name chattercontainer dennishk/asd-chatter-directory-server-submarine