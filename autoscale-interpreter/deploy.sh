mvn clean install -f ../autoscale-interface/pom.xml
mvn clean install docker:build
docker push registry.openshift.local/usecase/autoscale-interpreter
