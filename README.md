
# binding-tariff-ruling-frontend

A Publicly accessible frontend service used by Import/Exporters and Trade Consultants to research past BTI Rulings

##### Starting the Service
1) Launch dependencies `sm --start DIGITAL_TARIFF_DEPS -r`
2) Launch the back end `sm --start BINDING_TARIFF_CLASSIFICATION -r`
3) Launch the filetore `sm --start BINDING_TARIFF_FILESTORE -r`
4) Launch the service `sm --start BINDING_TARIFF_RULING_FRONTEND -r`

##### Populating data
This service is publically accessible, therefore populating its database is locked down. Cases must exist in the back end  `binding-tariff-classification` which can then be copied accross by calling `POST /ruling/:id`  where the `:id` is the case reference in the back end.

This endpoint is called by `tariff-classification-frontend` when a BTI Case is completed, and  `binding-tariff-admin-frontend` when a case is migrated.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
