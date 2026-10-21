### How to start the app

Building the docker image

```
docker build -t mortgage-matcher .
```

Run the image and start a container

```
docker run --rm --name mortgage-matcher -p 8080:8080 mortgage-matcher
```

docker internal port: 8080
exposed port: 8080
container removes when closed
