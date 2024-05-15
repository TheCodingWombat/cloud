#!/usr/bin/env bash

# Install packags.
sudo apt-get -y update
sudo apt-get -y upgrade
sudo apt-get install
sudo apt-get -y install openjdk-11-jdk git nmap vim maven jq python python-pip
sudo ln -sf /usr/share/zoneinfo/UTC /etc/localtime # Change localtime to UTC
sudo apt-get autoremove
sudo apt-get autoclean
sudo pip install awscli

# Installing Intel PIN Instrumentation Tool
PIN_PACKAGE=pin-3.22-98547-g7a303a835-gcc-linux.tar.gz
sudo mkdir -p /opt/pin/
wget https://software.intel.com/sites/landingpage/pintool/downloads/$PIN_PACKAGE
sudo tar zxvf $PIN_PACKAGE -C /opt/pin/ --strip-components 1
rm $PIN_PACKAGE
sudo chown -R root:root /opt/pin/
sudo chmod -R 755 /opt/pin/
sudo ln -s /opt/pin/pin /usr/bin/pin
echo "CNV VM Setup Finished! \o/"
