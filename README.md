# A simple UseCase to test Autoscalers

*This is a strongly simplified and headless Webshop for testing Autoscalers. It's built on the basics of a Microservice-Architecture and is being configured to be easily deployable on Docker-Based infrastructure (tested only on Openshift)*

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
![Alt DataSource](readme-resources/Grafana-DataSource-Config.png?raw=true "DataSource")

Import the predefined dashboard, which you can find in the root folder, named *Grafana-dashboard.json* . It looks like this:
![Alt DataSource](readme-resources/Grafana-Autoscaler-Dashboard-v2.png?raw=true "DataSource")

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
Execute a REST POST and checkout one of the predefined shopping-carts. Possible values are 0, 1, 2 or 3:

    POST http://frontendservice.openshift.local/checkout/1  