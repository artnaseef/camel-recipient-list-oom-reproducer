# Reproducer for RecipientList OOM

## To Use

$ mvn clean install camel:run
$ curl 'http://localhost:9000/start-test?start=1&count=10000'
$ curl 'http://localhost:9000/start-test?start=10001&count=10000'

* Run with low heap size to reproduce the OOM faster:
$ MAVEN_OPTS=-Xmx512M mvn clean install camel:run

* View the histogram
$ jmap -histo <pid> | grep LogEndpoint
$ jmap -histo $(jps -v | grep -i reproducer-for-my-issue | awk '{ print $1 }') | grep LogEndpoint

## OOM

* Running enough unique IDs through the start-test endpoint will eventually lead to an OOM condition.
* Each run of the test generates IDs from start through start + count.
* Running the same ID more than once does not contribute to the OOM condition.
