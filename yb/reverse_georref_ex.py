#!/usr/bin/python -tt
#coding=utf-8

# Author: Rui Figueira, Yuri
#
# File: reverse_georref.py
# This file takes geographic coordinates in decimal formal and makes a reverse
# georreferencing on OpenStreetMap to returns an object with the values for
# the following Darin Core terms: locality, municipality, stateProvince, country
# and countryCode.
#
# Note: The reverse georreferencing was customised for the geographic context of Portugal,
# in which we use the term stateProvince to report districts.
#
# Created on 25-02-2021 by Rui Figueira
# Extended on 22-0-2021 by Yuri


# python -Xutf8 reverse_georref_ex.py input_csv output_csv

import json
import requests
import os,sys,platform
import csv
import time

useragent="Python/3.0 (%s, %s, %s) %s/%s (+%s, Python/%s, %s, requests/%s)"%(
  platform.system(),platform.architecture()[0],platform.machine(),
  'revgeo','0.1','yb@',platform.python_version(), platform.python_implementation(),requests.__version__)

headers={
    "User-Agent": useragent,
    "Accept-Language": "en-US,en;q=0.5",
    "Accept": "application/json, text/javascript, */*; q=0.01",
    'Accept-Encoding': 'deflate, gzip',
}
cachefilename='reverse_georref_cache.json'
#load cache
cache={}
countresolved=0
if os.path.isfile(cachefilename):
  with open(cachefilename) as json_file:
    cache = json.load(json_file)


session=requests.Session()
#session.proxies={'https':'socks5h://192.168.88.1:8088'}
#%%
def tocoordidxstring(lat,long):
    latmnt,latsec = divmod(abs(lat)*3600,60)
    latdeg,latmnt = divmod(latmnt,60)
    lonmnt,lonsec = divmod(abs(long)*3600,60)
    londeg,lonmnt = divmod(lonmnt,60)
    return '%c%02d%02d%02d%c%03d%02d%02d'%('-' if lat<0 else '+',latdeg,latmnt,latsec,'-' if long<0 else '+',londeg,lonmnt,lonsec)

#%%
def storecache():
    with open(cachefilename, 'w') as f:
       json.dump(cache, f, ensure_ascii=False, indent=2)
#%%
def getReverseGeorref(lat, long):
    global countresolved
    idx=tocoordidxstring(lat,long)
    if idx in cache:
        result=cache[idx]
        print('.', end='')
    else:
        url = 'https://nominatim.openstreetmap.org/reverse.php?lat={}&lon={}&zoom=10&format=jsonv2&accept-language=en'.format(lat, long)
        time.sleep(1)
        txt='';
        try:
            txt = session.get(url,headers=headers)
            parsed_json = json.loads(txt.text)
        except Exception as e:
            sys.stderr.write('Fail nomination API: %s\n'%str(e))
            return None
    
        #name = parsed_json.get('name')
        address=parsed_json.get('address')
        if address is None:
            address={}
        display_name = parsed_json.get('display_name')
        municipality = address.get('municipality')
        town = address.get('town')
        county = address.get('county')
        country = address.get('country')
        country_code = address.get('country_code')
    
        # openstreetmap not always has the same administrative levels available
        # on town/municipality level. It is needed to find which is one is available
        # and use it\
    
        if municipality == None and town != None:
            municipality = town
        if municipality is not None:
          prev = display_name.split(municipality)
          local = prev[0]
        else:
          local=''
        if (len(local) == 0 and municipality is not None):
            local = municipality
    
        local = local.rstrip()
        local = local.rstrip(',')
    
        if municipality == None:
            municipality = ''
    
        if county == None:
            county = ''
    
        result = {"locality": local, "municipality": municipality, "stateProvince": county, "country": country,"country_code": country_code}
        cache[idx]=result
        countresolved+=1
        if countresolved%10==0:
            storecache()
            print('+', end='', flush=True)
        else:
            print(':', end='', flush=True)
        
    return(result)

#%%
 
if len(sys.argv) > 1:
    inpfilename=sys.argv[1]
else:
    inpfilename='coordinates.csv'
print("Using for input '%s'"%inpfilename)
    
if len(sys.argv) > 2:
    outfilename=sys.argv[2]
else:
    outfilename='coordinates_ref.csv'
print("Using for output '%s'"%outfilename)

#%%
with open(inpfilename, 'rU') as csvfile:
    dialect = csv.Sniffer().sniff(csvfile.read(10240))
    print("Detected csv input: separator:%s, quote:%s, eol:%s"%(repr(dialect.delimiter),repr(dialect.quotechar),repr(dialect.lineterminator)))
    csvfile.seek(0)
    reader = csv.reader(csvfile, dialect)
    inpdata=[row for row in reader]
#%%
inpdata[0].extend(["locality","municipality","stateProvince","country","country_code"])

for i in range(1,len(inpdata)):
    c_row=inpdata[i]
    try:
      lat=float(c_row[1].replace(',','.').strip())
      lon=float(c_row[2].replace(',','.').strip())
      resolved=getReverseGeorref(lat, lon)
      if None==resolved:
          c_row.extend([None,None,None,None,None])
      else:
          c_row.extend([resolved["locality"],resolved["municipality"],resolved["stateProvince"],resolved["country"],resolved["country_code"]])
          c_row[1]=lat
          c_row[2]=lon
    except Exception as e:
     c_row.extend([None,None,None,None,None])
     sys.stderr.write('Fail parse float on line %d: %s\n'%(i,str(e)))
    if i%80==0:
        print()

#%%
storecache()
print()
print('Done. %d lines processed, %d queries, %d in cache'%(len(inpdata)-1,countresolved,len(cache)))
with open(outfilename, 'w', newline='') as f:
    mywriter = csv.writer(f, dialect)
    mywriter.writerows(inpdata)
