
# binding-tariff-ruling-frontend

The frontend for the public Search For Advance Tariff Rulings service, use by importers, exporters and trade consultants to research ATaR rulings that are currently in effect.

### Running

##### To run this Service you will need:

1) [Service Manager](https://github.com/hmrc/service-manager) installed
2) [SBT](https://www.scala-sbt.org) Version `>=1.x` installed
3) [MongoDB](https://www.mongodb.com/) version `>=3.6` installed and running on port 27017
4) [Localstack](https://github.com/localstack/localstack) installed and running on port 4572
5) Create an S3 bucket in localstack by using `awslocal s3 mb s3://digital-tariffs-local` within the localstack container

The easiest way to run MongoDB and Localstack for local development is to use [Docker](https://docs.docker.com/get-docker/).

##### To run MongoDB

```
> docker run --restart unless-stopped -d -p 27017-27019:27017-27019 --name mongodb mongo:3.6.13
```

##### To run Localstack and create the S3 bucket

```
> docker run -d --restart unless-stopped --name localstack -e SERVICES=s3 -p4572:4566 -p8080:8080 localstack/localstack
> docker exec -it localstack bash
> awslocal s3 mb s3://digital-tariffs-local
> exit
```

##### Starting the service
1) Launch dependencies using `sm --start DIGITAL_TARIFF_DEPS -r`
2) Start the backend service using `sm --start BINDING_TARIFF_CLASSIFICATION -r`
3) Start the filestore service using `sm --start BINDING_TARIFF_FILESTORE -r`

Use `sbt run` to boot the app.

This application runs on port 9586.

Open `http://localhost:9586/search-for-advance-tariff-rulings`.

You can also run the `DIGITAL_TARIFFS` profile using `sm --start DIGITAL_TARIFFS -r` and then stop the Service Manager instance of this service using `sm --stop BINDING_TARIFF_RULING_FRONTEND` before running with sbt.

##### Populating data

Rulings that have been granted are stored in the backend service  [binding-tariff-classification](https://github.com/hmrc/binding-tariff-classification). When a case is completed with a decision to grant a ruling or when a ruling is cancelled early, the [tariff-classification-frontend](https://github.com/hmrc/tariff-classification-frontend) notifies this service, which prompts the service to refresh the ruling data for that case.

This service also features two scheduled jobs which run daily for refreshing the data for newly granted rulings and cancelled rulings. This is to ensure that ruling data is refreshed even in the event that the service was not able to be notified by the [tariff-classification-frontend](https://github.com/hmrc/tariff-classification-frontend).

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
