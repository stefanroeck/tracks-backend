# Hiking Tracks Backend

### State: WIP

A Kotlin/JVM based backend to expose my hiking tracks to the frontend application (see https://gpxtracks.vercel.app/
and https://github.com/stefanroeck/gpxtracks)
This app will roughly do the following

- Search for tagged hiking tracks in my Dropbox account (tag *#longdistancewalk*)
- Convert the raw FIT files to GPX (or similar format)
- Process the track data for single usage (elevation view) and bulk data (map overview)
- Make this performant, e.g. by leveraging some caching

# Development

### Build

`mvn install`

### Deployment

The following configuration parameters need to be provided

| Parameter Name       |
|:---------------------|
| dropbox.clientId     |
| dropbox.secret       |
| dropbox.refreshToken |
