mvn clean install -f ../autoscaler-interface/pom.xml
mvn clean install docker:build
docker push registry.openshift.local/usecase/metrics-interpreter
