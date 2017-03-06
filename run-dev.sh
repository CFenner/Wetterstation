#!/bin/bash
#TODO: check which flags are really needed :-)
docker run \
    --privileged \
    --rm \
    -it \
    --cap-add SYS_RAWIO \
    --device /dev/mem \
    -v /var/anemo:/anemo \
    -v /root/.gradle:/root/.gradle \
    -p 80:80 \
    -p 8080:8080 \
    anemo \
    ./gradlew --info execute
