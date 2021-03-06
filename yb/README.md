
Here are my scripts

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

The program prints information about the input and output files and a ‘`:`’ on each query performed, ‘`+`’ when the cache file is updated and a ‘`.`’ for each coordinate processed from the cache. The cache file is in JSON format and is loaded from the current directory or created if it does not exist.


## coltojson.py
Convert values from various columns in CSV to single JSON string to be used DwC:dynamicProperties

Example:
```python3 coltojson.py ex_coltojson_inp.csv ex_coltojson_out.csv```

## unpivot_csv.py
Performs reverse pivot on a table selecting columns to be used for keys.

Example:
```python3 unpivot_csv.py 1,2,9,10 table.csv table_unpivot.csv```

- list of columns numbers to be used for key, numbers only, coma separated, no spaces (default “1”);
- input csv table file name (default “table.csv”);
- ouput csv table file name (default “table_unpivot.csv”);
## timecoord_to_iso.py
Re-formats date and time columns to DwC ISO format optionally adding correct timezone from coordinates if available.

Example:
```python3 timecoord_to_iso.py ex_timecoord_inp.csv ex_timecoord_out.csv```

Input columns:

 1. Index (integer)
 2. Verbatim Date (`YYYY.MM.DD` or `DD.MM.YYYY`, separator is any of `.`, `/`, `-` or *`space`* )
 3. Time in euring format (`HHmm` or `HH--`)
 4. Decimal Latitude
 5. Decimal Longitude

Input columns are copied to output adding the ISO datetime, date and time (with or without time offset) as last 3 columns.

## dupocup_ex.py
creates event-occurrence-resourcerelationship from ocupprici and ocupsegund

## OCR_Table.py
Temporary OCR/table implementation that uses on tesseract dynamic library instead of executable
- more info https://pypi.org/project/tesserocr/
- implemented commandline settings for input and output files
- uses LSTM pre-trained networks - gives sligthly betted results
- will crop the individual images by one extra pixel to reduce sell frames artifacts
- removes redundant EOL, CR and spaces from the cells

## hocrtransform.py
Tool to convert list of .hocr and corresponding .jpg files to searchable .pdf
- modified from ocrmypdf/hocrtransform
- Combine multiple HOCRs in single PDF
- Add image layer if JPG with same name exisis
- copies some metadata from the first JPG to the output PDF
- accepts option to choose another standard PDF font instead of 'Helvetica'