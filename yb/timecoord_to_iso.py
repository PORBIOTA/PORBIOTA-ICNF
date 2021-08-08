#!/usr/bin/python -tt
#coding=utf-8
import json
import requests
import os,sys,platform
import csv
import datetime,time,pytz
import re

useragent="Python/3.0 (%s, %s, %s) %s/%s (+%s, Python/%s, %s, requests/%s)"%(
  platform.system(),platform.architecture()[0],platform.machine(),
  'timecoordiso','0.1','yb@',platform.python_version(), platform.python_implementation(),requests.__version__)

headers={
    "User-Agent": useragent,
    "Accept-Language": "en-US,en;q=0.5",
    "Accept": "application/json, text/javascript, */*; q=0.01",
    #'Accept-Encoding': 'deflate, gzip',
}
cachefilename='coord_timezone_cache.json'
#load cache
cache={}
countresolved=0
if os.path.isfile(cachefilename):
  with open(cachefilename) as json_file:
    cache = json.load(json_file)


session=requests.Session()
#session.proxies={'https':'socks5h://192.168.88.1:8088'}
#%%
reDateDMY=re.compile('^\s*(\d|[012]\d|3[01])[\.\-/ ](\d|[0]\d|[1][012])[\.\-/ ](\d\d|[12]\d\d\d)\s*$')
reDateYMD=re.compile('^\s*([12]\d\d\d)[\.\-/ ](\d|[0]\d|[1][012])[\.\-/ ](\d|[012]\d|3[01])\s*$')
reTime=re.compile('^\s*(2[0123]|1[\d]|0[\d]|\d)([0-5]\d|--)\s*$')
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
def gettimezonename(lat, long):
    global countresolved
    idx=tocoordidxstring(lat,long)
    if idx in cache:
        tzname=cache[idx]
        print('.', end='')
    else:
        url = 'https://timezonefinder.michelfe.it/api/0_%0.7f_%0.7f'%(long,lat)
        #print(url)
        time.sleep(0.2)
        txt='';
        try:
            txt = session.get(url,headers=headers)
            #print(txt.text)
            parsed_json = json.loads(txt.text)
        except Exception as e:
            sys.stderr.write('Fail timezonefinder API: %s\n'%str(e))
            return None
        tzname=parsed_json.get('tz_name')
        stcode=parsed_json.get('status_code')
        if not 200 == stcode:
            sys.stderr.write('Bad status timezonefinder API: %s\n'%stcode)
            return None
            
        cache[idx]=tzname
        countresolved+=1
        if countresolved%10==0:
            storecache()
            print('+', end='', flush=True)
        else:
            print(':', end='', flush=True)
        
    return(tzname)

#%%
 
if len(sys.argv) > 1:
    inpfilename=sys.argv[1]
else:
    inpfilename='datetimecoord.csv'
print("Using for input '%s'"%inpfilename)
    
if len(sys.argv) > 2:
    outfilename=sys.argv[2]
else:
    outfilename='datetimezone.csv'
print("Using for output '%s'"%outfilename)

#%%
with open(inpfilename, 'r') as csvfile:
    dialect = csv.Sniffer().sniff(csvfile.read(10240))
    print("Detected csv input: separator:%s, quote:%s, eol:%s"%(repr(dialect.delimiter),repr(dialect.quotechar),repr(dialect.lineterminator)))
    csvfile.seek(0)
    reader = csv.reader(csvfile, dialect)
    inpdata=[row for row in reader]
#%%
inpdata[0].extend(["eventDate"])

for i in range(1,len(inpdata)):
    c_row=inpdata[i]
    try:
      lat=float(c_row[3].replace(',','.').strip())
      lon=float(c_row[4].replace(',','.').strip())
      tzname=gettimezonename(lat, lon)
      timezone=pytz.timezone(tzname)
    except pytz.exceptions.UnknownTimeZoneError as e:
      print(str(e))
      timezone=False
    m=reDateDMY.match(c_row[1])
    has_date=False
    if m:
        day=int(m.group(1))
        mon=int(m.group(2))
        yea=int(m.group(3))
        has_date=True
    else:
        m=reDateYMD.match(c_row[1])
        if m:
            day=int(m.group(3))
            mon=int(m.group(2))
            yea=int(m.group(1))
            has_date=True
    has_time=False
    m=reTime.match(c_row[2])
    if m:
        hou=int(m.group(1))
        has_time=True
        if m.group(2)=='--':
            mnt=0
        else:
            mnt=int(m.group(1))
    if has_date:
        if has_time:
            n=datetime.datetime(yea,mon,day,hou,mnt,0)
            try:
                timezone=pytz.timezone(tzname)
            except UnknownTimeZoneError:
                timezone=False
            if timezone:
                res=timezone.localize(n).replace(microsecond=0).isoformat()
            else:
                res=n.isoformat()
        else:
             n=datetime.date(yea,mon,day)
             res=n.isoformat()
    else:
        res=None
    c_row.extend([res])
    if i%80==0:
        print()

#%%
storecache()
print()
print('Done. %d lines processed, %d queries, %d in cache'%(len(inpdata)-1,countresolved,len(cache)))
with open(outfilename, 'w', newline='') as f:
    mywriter = csv.writer(f, dialect)
    mywriter.writerows(inpdata)
