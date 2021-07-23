#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Jul 23 10:01:31 2021

@author: yb
"""

import json
import sys
import csv

#%%
 
if len(sys.argv) > 1:
    inpfilename=sys.argv[1]
else:
    inpfilename='input.csv'
print("Using for input '%s'"%inpfilename)
    
if len(sys.argv) > 2:
    outfilename=sys.argv[2]
else:
    outfilename='output.csv'
print("Using for output '%s'"%outfilename)

with open(inpfilename, 'rU') as csvfile:
    dialect = csv.Sniffer().sniff(csvfile.read(10240))
    print("Detected csv input: separator:%s, quote:%s, eol:%s"%(repr(dialect.delimiter),repr(dialect.quotechar),repr(dialect.lineterminator)))
    csvfile.seek(0)
    reader = csv.reader(csvfile, dialect)
    inpdata=[row for row in reader]

print("columnnames: %s"%','.join([inpdata[0][n] for n in range(1,len(inpdata[0]))]))

#%%
def datatojson(colnames,data):
    res={}
    for i in range(1,len(colnames)):
        n=colnames[i]
        v=data[i]
        if v:
            res[n]=v
    return json.dumps(res, ensure_ascii=False, indent=None)
#%%
outdata=[[inpdata[0][0],"dynamicProperties"],]
outdata.extend([[inpdata[n][0],datatojson(inpdata[0],inpdata[n])] for n in range(1,len(inpdata))])
#%%
print('Done. %d lines processed.'%(len(outdata)-1))
if not dialect.escapechar:
    dialect.escapechar='"'
with open(outfilename, 'w', newline='') as f:
    mywriter = csv.writer(f, dialect)
    mywriter.writerows(outdata)
