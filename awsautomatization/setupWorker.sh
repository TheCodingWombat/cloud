#!/bin/bash

source ~/.aws/config.sh

cmd=$(cat <<EOF
sudo yum update all -y; 
sudo yum install java-17-amazon-corretto -y;
sudo yum install git -y;
git config --global user.name "demo";
git config --global user.email "demo";
git clone https://github.com/TheCodingWombat/cloud.git;
sudo wget https://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo;
sleep 10;
sudo sed -i 's/\$releasever/6/g' /etc/yum.repos.d/epel-apache-maven.repo;
sleep 10;
sudo yum install -y apache-maven;
cd cloud;
mvn -f javaagent/pom.xml clean package;
mvn -f cnv24-g22-master/pom.xml clean package;
nohup java -cp cnv24-g22-master/webserver/target/webserver-1.0.0-SNAPSHOT-jar-with-dependencies.jar -javaagent:javaagent/target/JavassistWrapper-1.0-jar-with-dependencies.jar=MethodExecutionTimer:pt.ulisboa.tecnico.cnv:output pt.ulisboa.tecnico.cnv.webserver.WebServer > /tmp/webserver.log 2>&1 &
EOF
)
ssh -o StrictHostKeyChecking=no -i "~/.aws/newkey.pem" ec2-user@$(cat outputs/instance.dns) $cmd > outputs/ssh_output.log 2>&1