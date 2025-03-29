#!/bin/bash

mkdir ./app

cd ./app

git clone $REPOSITORY_URL .

/bin/sh ./gradlew build
/bin/sh  ./gradlew bootRun
