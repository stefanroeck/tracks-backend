# Hiking Tracks Backend

### State: WIP

A Kotlin/JVM based backend to expose my hiking tracks to the frontend application (see https://gpxtracks.vercel.app/
and https://github.com/stefanroeck/gpxtracks)
This app will roughly do the following

- :white_check_mark: Search for tagged hiking tracks in my Dropbox account (tag *#longdistancewalk*)
- :white_check_mark: Convert the raw FIT files to GPX
- :white_check_mark: Process the track data for single usage (elevation view) and bulk data (map overview)
- :white_check_mark: Make this performant, e.g. by leveraging some caching
- :white_check_mark: Switch from Mongo to lightweight DB and write DB Integration tests
- Also older process TCX files from Dropbox
- Fetch Weather data and store in DB instead of fetching in UI
- Migrate to Gradle for faster builds

# Development

### Build

`mvn install`

### Deployment

- Browser APIs at http://localhost:8082/swagger-ui/index.html

## Application

The following configuration parameters need to be provided to run the application

| Parameter Name       |
|:---------------------|
| dropbox.clientId     |
| dropbox.secret       |
| dropbox.refreshToken |
| db.username          |
| db.password          |
