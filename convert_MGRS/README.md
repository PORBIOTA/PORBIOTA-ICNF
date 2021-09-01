# Convert Military Grid Reference System (MGRS) UTM coordinates to Geographic coordinates

This python script converts coordinates in MGRS format to geographic coordinates (latitude and longitude).

## Prepare to run
### 1. Prequesits
You need to have python installed in your system in order to run this script. You also need to install 
the **MGRS** module, which will be used to convert coordinates. To install this module, the easiest way 
is to use **pip** (you may have to install it as well). To install, do `pip install MGRS`.

### 2. Prepare input files
- download the script `toLatlon.py` to your computer's folder were you will run the script
- the coordinates to be converted should be in a **csv file**, which should use a comma as separator
- bydefault, the input file should be called __coord.csv__, but you can provide a different filename
  with the parameter -f
- the first two coordinates of the input csv file should be an ID column and the column with the coordinates.
  in MGRS format. Any other columns will be ignored.
- the first line should have the column names
- the coordinate should include the UTM zone reference. For example, for Portugal mainland it will be **29S**
  or **29T**. A full coordinate it have the format **29SND3656**, for a UTM MGRS reference based on a 1 km grid.
  The following is an example of the format of an input file
  

  |eventID |verbatimCoordinates |locality |
    ---------|-------------------|------------
  |1 |29SNC1502 | Estrada Nacional 261-3 |
  |2 |29SNC1703 | "Estrada para Porto Covo, mais ou menos a 3 km de Sines." |
  |3 |29SNC1904 | 2 km a nascente do local 2. |
  |4 |29SNC2306 | 100 m poente do Poço da Obra. |
  

## Run the script
If your input file is named `coord.csv`, run the script in your system's command line doing 

```python loLatLon.py```

In the case that you want to provide a different name for your input file, do 

```python loLatLon.py -f myfile.csv```


## Output
If everything worked as planed, the script will produce a file called __outLatLon.csv__ containing the original ID
values, and the latitude and longitude

## Remarks
There script does not make any transformation in the reference system of the coordinates. If you need the coordinates
in a different reference system of the input, you have to make the transformation on the obtained geographic
coordinates.
