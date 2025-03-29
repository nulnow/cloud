#!/bin/bash

mkdir ./app

cd ./app

git clone $REPO .

./gradlew build && ./gradlew bootRun
