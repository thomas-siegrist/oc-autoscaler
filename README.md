# An Openshift Autoscaler prototype

*This prototype provides very specific autoscaling of services running on openshift, based on various self-defined metrics. We wrote this code during our Semesters-Work. We ended up with a prototype that delivers surprisingly nice results, considering how little time we were to spend. Yet it is still in prototype shape and needs some refactoring, testing and improvements in order to be production-redy. The code is quick n' dirty, functionality is way better than expected. Have fun and be free to contribute if you have some valuable inputs =) !!*

## Deployment on Openshift
#### Prerequisites
* Docker installed
* Git installed
* OC cli installed
* Docker-Hub account

* Local Openshift installation
* An Openshift Project to test the autoscaler on.

> Use Openshift with the internal Docker-Registry listening on registry.openshift.local -> map this url to the IP of Openshift's internal registry in your /etc/hosts file if necessary)

#### Setup InfluxDB and configure Grafana
Login Docker Docker-Hub with your account:

    docker login hub.docker.com

Pull InfluxDB, tag it and push it to the local Openshift registry:

    docker pull tutum/influxdb
    docker tag tutum/influxdb registry.openshift.local/usecase/influxdb
    docker login registry.openshift.local
    docker push registry.openshift.local/usecase/influxdb

Login to Openshift, choose your project and setup an influxdb service:

    oc login
    oc project usecase
    oc new-app usecase/influxdb

Expose the InfluxDB service (Port 8086) in order to be accessible from outside of openshift. We used Grafana to visualize the data in a dashboard.

Connect a Grafana instance to the exposed InfluxDB Service:
![Alt DataSource](readme-resources/Grafana-DataSource-Config-v2.png?raw=true "DataSource")

Import the predefined dashboard, which you can find in the root folder, named *Grafana-dashboard.json* . It looks like this:
![Alt DataSource](readme-resources/Grafana-Autoscaler-Dashboard.png?raw=true "DataSource")

#### Setup Autoscaler
    git clone https://github.com/thomas-siegrist/oc-autoscaler.git
    cd oc-autoscaler
    docker login registry.openshift.local

Build and deploy all services as Docker images to the Docker registry with the deploy-scripts in the projects folders:

    cd ./autoscale-interpreter
    ./deploy.sh
    cd ../autoscale-scaler
    ./deploy.sh

Create all the services in openshift:

    oc new-app {projectname}/autoscale-interpreter
    oc new-app {projectname}/autoscale-scaler

The Autoscale Interpreter has a Rest-Service and an according Swagger-UI that allows us to configure the Autoscale Interpreter in running state. In order to be accessible, expose the service over autoscale-interpreter.openshift.local

Set the spring Stage of all the services via Environment-Variable to dev:

* Go to the Openshift console and click Browse / Deployments
* Click on the service (do this for each service separately)
* Click on Actions / Edit Yaml
* Scroll down to the entry called "Protocol TCP"

Insert the following code-snippet right after it:

    env:
		-
			name: STAGE
			value: dev 

#### Test the setup
Open the Grafana UI, produce some load and watch the metrics and pods going up and down.