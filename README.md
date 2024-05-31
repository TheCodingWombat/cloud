# Cloud Computing Project - Group 22
## Folder Structure 
Our project consits of the following folder structure:
- awsautomatization - all of our automatization scripts for deploying EC2 instnaces, seting up a machine to create a AMI, Lambda de-/registration
- cnv24-g22-master - origninal repository provided by IST Lisboa for our Webservice code
- javaagent - consists of our instrumentation code which is based on Javaasist. That helps us to extract the needed metrics from our worker VMs.
- testScripts - are python scripts which helps us to test the LoadBalancer for scaling up and down
- LoadBalancer - is our LoadBalancer functionality which consists of the Austoscaler, AwsEC2Manager and more.

All of the Java projects are Maven based