#!/bin/bash

mkdir ./app

cd ./app

git clone $REPO .

./gradlew build && java -jar build/libs/TODO.jar
