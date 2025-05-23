-- Create the table for storing TrackEntity
CREATE TABLE IF NOT EXISTS track (
    internal_track_id BIGINT AUTO_INCREMENT PRIMARY KEY,   -- Auto-increment trackId as BIGINT
    track_id VARCHAR(255) NOT NULL UNIQUE,        -- trackId as logical key
    track_name VARCHAR(255) NOT NULL,         -- trackName
    dropbox_id VARCHAR(255) NOT NULL,         -- dropboxId
    track_timestamp TIMESTAMP NOT NULL,      -- trackTimestamp (Instant)
    total_elapsed_time REAL NOT NULL,
    total_timer_time REAL NOT NULL,
    total_distance REAL NOT NULL,
    total_ascent INTEGER NOT NULL,
    total_descent INTEGER NOT NULL,
    total_calories INTEGER NULL,
    min_lat DOUBLE NOT NULL,                 -- Bounds: minLat
    max_lat DOUBLE NOT NULL,                 -- Bounds: maxLat
    min_lon DOUBLE NOT NULL,                 -- Bounds: minLon
    max_lon DOUBLE NOT NULL,                 -- Bounds: maxLon
    temperature VARCHAR(255) NOT NULL,
    weather_symbol VARCHAR(255) NOT NULL,
    gpx_data_original_xml CLOB,              -- gpxDataOriginalXml (XML data)
    gpx_data_preview_xml CLOB,               -- gpxDataPreviewXml (XML data)
    gpx_data_detail_xml CLOB                 -- gpxDataDetailXml (XML data)
);

