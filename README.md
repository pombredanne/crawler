Site crawler application
=======

This application can be used to crawl sites with articles and to add some processing for each article. 
You need to maintain the list of sites, all the other things will be done automatically.
Application built using [aws-java-sdk](http://aws.amazon.com/sdkforjava/), custom [amazon-logger](https://github.com/vmorev/amazon-logger) and custom diffbot java client

Code is available under Apache 2.0 license.

Prerequirements are to have:
- [DiffBot](http://www.diffbot.com/) account
- [Amazon AWS](http://aws.amazon.com) account


Compilation can be done with maven by calling:
```bash
maven clean install
```

You will need to do some configuration for tests to pass othervise you could just turn off tests:
```bash
mvn clean install -Dmaven.test.skip=true
```

The configuration required is about to put your credentials into config files:

###src/main/resources/aws.json
```
{
    "accessKey" : "YOUR_KEY",
    "secretKey" : "YOUR_SECRET_KEY",
...
}
```
###src/main/resources/diffbot.local.json
```
{
    "token" : "YOUR_TOKEN"
}
```
###src/main/resources/log4j.local.properties
```
...
log4j.appender.s3logger=com.github.vmorev.amazon.log4j.S3Logger
log4j.appender.s3logger.accessKey=YOUR_KEY
log4j.appender.s3logger.secretKey=YOUR_SECRET_KEY
log4j.appender.s3logger.s3logBucket=LOGS_BUCKET
log4j.appender.s3logger.batchSize=BATCH_SIZE
```

