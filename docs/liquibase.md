# Liquibase Migrations
Using Liquibase migrations allows you to make changes to your database tables without having to restart the entire database.

Liquibase allows you to describe changes to the database schema and it uses those descriptions to apply changes to it.

The changes can be described in JSON and are stored in `src/resources/db/migration/changelog/changes`.

Each file is a description of the database changes to each of the defined entities. For all possible changes you can do to the database you can check: https://docs.liquibase.com/change-types/home.html

The key here is consistance, always create the database changelog file after you create a new entity or modify it.

## Commands

### Check changes to database
```
mvn liquibase:updateSQL
```
This command will show the changes that will be applied when following the change log you described. This will not apply the changes just provide you with a way to inspect it

### Apply chnages to database
```
mvn liquibase:update
```
This command will apply the changes to the database. The changes will also be applied when you start your application so you wont need to update it before starting the app.

### Sync database to changelog
```
mvn liquibase:changelogSync
```
This command will make all the changes you currently have be marked as applied already, this is useful when working on a database that already exists, when you don't want for example to create a table that already exists.

### Tagging a version of the database
```
mvn liquibase:tag --tag=myTag
```
This can be used to mark a version of the database that can be used when doing rollbacks

### Rollback to tag
```
mvn liquibase:rollback --tag=myTag
```
This command can be used to rollback the database to a previousily marked tag

## Preconditions
```
"changeSet": {
    "preConditions": [
      {
        "onFail": "MARK_RAN"
      },
      {
        "not": [
          {
            "tableExists": {
              "tableName": "MY_NEW_TABLE"
            }
          }
        ]
      }
    ]
    "changes": etc
    }
``` 
Preconditions can be set in each of the changes that will help you determine if certain change should be ran or what to do if it fails.

The example above tells liquibase to mark a change as ran in case it fails because the table already exists, very useful when working with an already existing database.

For more information on preconditions you can see https://docs.liquibase.com/concepts/changelogs/preconditions.html