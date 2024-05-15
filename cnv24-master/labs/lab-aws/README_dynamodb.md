## Project Support (extra): Amazon DynamoDB 

---

Amazon DynamoDB is a NoSQL database service that supports key–value and document data structures and is ran by Amazon, as part of the Amazon Web Services portfolio. DynamoDB can be used without any previous setup (i.e., it has no fixed infrastructure managed by the client), and can be used directly through a number of AWS produces such as EC2 and Lambda.

Conside the Java source code example available in [AmazonDynamoDBSample.java](https://gitlab.rnl.tecnico.ulisboa.pt/cnv/cnv24/-/blob/master/labs/lab-faas/res/AmazonDynamoDBSample.java).

Notes:

- when compiling or invoking a Java application that interacts with AWS, always include the SDK in the classpath (the exact `jar` name and version might change):

    - `javac -cp <path-to-aws-sdk>/lib/aws-java-sdk-1.12.196.jar:<path-to-aws-sdk>/third-party/lib/*:. <Java file>`

    - `java -cp <path-to-aws-sdk>/lib/aws-java-sdk-1.12.196.jar:<path-to-aws-sdk>/third-party/lib/*:. <Main Class>`

- remember to load the [config.sh](../lab-aws/scripts/config.sh) into your environment. This file contains credentials necessary for the SDK to connect to your AWS account:

    - `source config.sh`


---

