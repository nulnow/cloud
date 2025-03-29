#!/bin/bash

mkdir ./app

cd ./app || exit

export NVM_DIR="$([ -z "${XDG_CONFIG_HOME-}" ] && printf %s "${HOME}/.nvm" || printf %s "${XDG_CONFIG_HOME}/nvm")" || exit
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" || exit # This loads nvm

nvm use 20 || exit



#git clone https://github.com/nulnow/my-react-ts-app . || exit
git clone $REPOSITORY_URL . || exit

npm i || exit
npm run build || exit

npm i -g serve || exit

npx serve ./dist/