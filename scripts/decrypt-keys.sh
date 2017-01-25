#!/bin/sh

openssl aes-256-cbc -K $encrypted_4177d4fd1dcb_key -iv $encrypted_4177d4fd1dcb_iv -in travis-deploy-key.enc -out travis-deploy-key -dñ
chmod 600 travis-deploy-key;
cp travis-deploy-key ~/.ssh/id_rsa;

