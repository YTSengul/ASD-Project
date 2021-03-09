#!/bin/bash

sudo apt install -y docker.io openjdk-11-jre-headless maven

mkdir ~/chatterDirServer
cd ~/chatterDirServer/
git clone ssh://git@94.124.143.192:7999/odzkjz/chatter_directory_server_submarine.git .
git checkout master