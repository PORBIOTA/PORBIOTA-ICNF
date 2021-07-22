Here will be my scripts

## reverse_georref_ex.py
Commandline tool to create reverse georeferencing data as defined in DwC from decimal coordinates using openstreetpam API, leveraging local cache to avoid unnecessary queries, and performs no more than one query per second. It takes as input CSV file with 3 columns (the header with column names must exist but is ignored, the number and order of the columns is important):
- IDX line number or index. Not used to perform the lookup, but will be included in the output as is. may be used as index to join back the table to the original
- Latitude: Latitude in decimal number
- Longitude: Longitude in decimal number

Takes one or two parameters:
- Input CSV file as generated above;
- (Optional) Output CSV file.

Example:
```python3 reverse_georref_ex.py ex_input.csv ex_output.csv```
The program prints information about the input and output files and a ‘:’ on each query performed, ‘+’ when the cache file is updated and a ‘.’ for each coordinate processed from the cache. The cache file is in JSON format and is loaded from the current directory or created if it does not exist.