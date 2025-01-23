# binding-tariff-ruling-frontend

The frontend for the public Search For Advance Tariff Rulings service, use by importers, exporters and trade consultants to research ATaR rulings that are currently in effect.

## Running

#### To run this Service you will need:

1) [Service Manager 2](https://github.com/hmrc/sm2) installed
2) [SBT](https://www.scala-sbt.org) Version `>=1.x` installed
3) [MongoDB](https://www.mongodb.com/) version `6.0` installed and running on port 27017
4) [Localstack](https://github.com/localstack/localstack) installed and running on port 4572
5) Create an S3 bucket in localstack by using `awslocal s3 mb s3://digital-tariffs-local` within the localstack container

The easiest way to run MongoDB and Localstack for local development is to use [Docker](https://docs.docker.com/get-docker/).

#### To run Localstack and create the S3 bucket

```
> docker run -d --restart unless-stopped --name localstack -e SERVICES=s3 -p4572:4566 -p8080:8080 localstack/localstack
> docker exec -it localstack bash
> awslocal s3 mb s3://digital-tariffs-local
> exit
```

#### Starting the service

Launch dependencies using `sm2 --start DIGITAL_TARIFFS`

If you want to run it locally:

- `sm2 --stop BINDING_TARIFF_RULING_FRONTEND`
- `sbt run`

This application runs on port 9586.

Open `http://localhost:9586/search-for-advance-tariff-rulings`.

## Testing

Run `./run_all_tests.sh`. This also runs coverage testing.

or `sbt test` to run the tests only.

### Populating data

Rulings that have been granted are stored in the backend service  [binding-tariff-classification](https://github.com/hmrc/binding-tariff-classification). When a case is completed with a decision to grant a ruling or when a ruling is cancelled early, the [tariff-classification-frontend](https://github.com/hmrc/tariff-classification-frontend) notifies this service, which prompts the service to refresh the ruling data for that case.

This service also features two scheduled jobs which run daily for refreshing the data for newly granted rulings and cancelled rulings. This is to ensure that ruling data is refreshed even in the event that the service was not able to be notified by the [tariff-classification-frontend](https://github.com/hmrc/tariff-classification-frontend).

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
