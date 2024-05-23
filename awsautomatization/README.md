# Setting up AWS Infrastructure
## Set up the enviroment - USE LINUX VM

While the AWS Web Console is intuitive and gives instant visual feedback upon interaction, it is not the best for repetitive interactions. E.g., imagine that you need to re-execute the previous walkthrough everytime you need to work on your course project! To automate the deployment process, we can use [AWS CLI](https://aws.amazon.com/cli/). 

To install it (you can skip the installation steps if you are using the CNV VM) you need to follow these steps (taken from the official [guide](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)):

- download `awscli-exe-linux-x86_64.zip` using `curl`:

    `curl -L https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip -o awscliv2.zip`

- extract it:

    `unzip awscliv2.zip`

- install it locally:

    `aws/install -i ~/aws-cli -b ~/aws-cli-bin`

- you can now delete the zipped and unzipped packages:

    `rm -r aws awscliv2.zip`

In addition, we will also need an additional tool, [jq](https://stedolan.github.io/jq/), a JSON processing tool. To install it:

- download `https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64` using `curl` into the `aws-cli-bin`:

    `curl -L https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64 -o ~/aws-cli-bin/jq`
 
After installing the `aws` and `jq` tools, the next step is to insert your AWS details into the [config.sh](scripts/config.sh) file:

In the `/home/user` there should be .aws folder, in not create one. Inside create `config.sh` file with the following variables:

- `AWS_ACCOUNT_ID` is available on your **AWS Web Console > IAM Dashboard**

- `AWS_ACCESS_KEY_ID` is available on your **AWS Web Console > Security Credentials** (section available by clicking your name on the top right);

- `AWS_SECRET_ACCESS_KEY` (same as the above);

- `AWS_EC2_SSH_KEYPAR_PATH` is the path to the `pem` file used in the previous walkthrough;

- `AWS_SECURITY_GROUP` is the name of the security group created in the previous walkthrough;

- `AWS_KEYPAIR_NAME` is the name of the key pair used in the previous walkthrough.

After setting up the configuration, there are two main scripts that will help you (automatically) reproduce the previous walkthough:

- [initializeServerLB.sh](reate-image.sh), launches 2 VMs, one with the Webserver and another with the LoadBalancer running on ports 8000 and 8001 respectivly;

- [terminateVM.sh](terminateVM.sh), terminates all deployed EC2 machines;