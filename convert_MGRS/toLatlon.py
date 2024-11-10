#!/bin/env python
'''
  Author: Rui Figueira

  Project: convertMGRS
  Convert coordinates in MGRS in a csv file to Lat Lon coordinates

  File: toLatLong.py

  Specifics:
  - uses the module MGRS to make conversios
  - the reference system of the input is the reference system of the output. You
  will need to make a transformation if a different reference system is wanted

  Assumptions:
  - an input csv file should be provided with a comma separator. By default,
  this file should be called "coord.csv", but you can provide a different filename
  with the parameter -f
  - the first two coordinates of the input csv file are an ID column and the
  coordinates column in MGRS format. Any other columns will be ignored.
  - the coordinate needs to have the zone reference. For example, for
  Portugal mainland it will be 29S or 29T, and the full coordinate it will have
  a format of 29SND3656, for a UTM MGRS reference based on a 1 km grid.
'''

import os
import sys
import mgrs
import csv
import argparse

m = mgrs.MGRS()

def convertToLatLon(coord_MG):
    try:
        latlon = m.toLatLon(coord_MG)
    except ValueError:
        print ("erro")
    return latlon

def main():
    parser = argparse.ArgumentParser(description = "Help on how to use this script ")
    parser.add_argument("-f", "--filename", help = "Name of the csv file containing \
    MGRS coordinates to be converted.", required = False, default = "coord.csv")

    argument = parser.parse_args()

    filename = argument.filename

    with open('./outLatLon.csv', 'w') as outFile:
        file_writer = csv.writer(outFile, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)

        with open(filename, 'r') as csv_file:
            csv_reader = csv.reader(csv_file, delimiter=',')
            line_count = 0
            for row in csv_reader:
                if line_count == 0:
                    file_writer.writerow(['ID','lat','lon'])
                if line_count > 0:
                    id = row[0]
                    latlon = convertToLatLon(row[1])
                    print (latlon)
                    file_writer.writerow([id,latlon[0],latlon[1]])
                line_count += 1



if __name__ == '__main__':
    main()
