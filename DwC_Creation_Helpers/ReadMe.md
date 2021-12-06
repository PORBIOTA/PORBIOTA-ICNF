# Helper Programs

The programs here avaiable consist on a series of tools to facilitate the transformation of a .shp file (or a .csv table) into a .csv table following the DwC standard.
They can be used in sequence or isolated, as appropriate.

All the provided programs require Java to run, with the exception of the R script.

Following is a short explanation of the functions offered by each of the programs:

****
**shp_to_csv_converter.r**

R script to convert a .shp file into a .csv with DwC terms while calculating the centroids (and precision) of the shapes in both the original CRS and in WGS84.
Additionaly, it cross references the obtained coordinates with the portuguese CAOP to extract the location name.

DwC terms outputted: verbatimLongitude, verbatimLatitude, verbatimSRS, decimalLongitude, decimalLatitude, geodeticDatum, coordinateUncertaintyInMeters, coordinatePrecision, stateProvince, municipality, locality

****
**LocationExtractor.zip**

Creates a .CSV with the results obtained by inputing WGS84 coordinates into the OpenStreetMaps API.

Useful to obtain, based on coordinates, the names of locations and their administrative divisions.

DwC terms outputted:  country, countryCode, locality, municipality, county, stateProvince, islandGroup

****
**MetadataExtractor.zip**

Creates a .TXT with the metadata info extracted from a .CSV darwinCore-like file.

Useful to obtain the necessary metadata to fill in the metadata field in the GBIF publishing platform.

****
**SpeciesExtractor.zip**

Creates a .CSV with the results obtained by cross referencing the species name in one .CSV with those from GBIF API.
Aditionally it creates an occurenceID for each occurence.

Useful to obtain the correct scientific names and the taxonomy of the species.

DwC terms outputted: scientificName, acceptedNameUsage, kingdom, phylum, class, order, family, genus, specificEpithet, infraspecificEpithet, taxonRank, scientificNameAuthorship, occurrenceID


****
**TimezoneExtractor.zip**

Creates a CSV with an added column with the time zone of the coordinates when compared to UTC.
This was compressed into a two part .zip because the file was too big (30mb).

Useful to convert time into standard UTC.

DwC terms outputted: eventDate (only the time)

****
**MGRStoLongLat.zip**

Creates a .CSV by converting MGRS coordinates to decimal Longitude and Latitude.

DwC terms outputted: decimalLongitude, decimalLatitude

****
**FixedColumnAdder.zip**

Creates a UTF-8 .CSV adding the initial GBIF fixed columns. 

Useful to add the necessary DwC fixed value columns.

DwC terms outputted: collectionCode, type, modified, license, institutionID, institutionCode, datasetName, basisOfRecord, occurrenceStatus

****
**Table Joiner.zip**

Creates a UTF-8 .CSV by merging two or more .CSVs together. 

Useful to join the outputs of the different programs together.
