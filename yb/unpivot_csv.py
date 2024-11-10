#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Jul 26 22:15:13 2021

@author: yb
"""

import sys
import csv

#%%
if len(sys.argv) > 1:
    keycolnums=[int(s) for s in sys.argv[1].split(',')]
else:
    keycolnums=[1,]
#%%
if len(sys.argv) > 2:
    inpfilename=sys.argv[2]
else:
    inpfilename='table.csv'
print("Using for input '%s'"%inpfilename)
    
if len(sys.argv) > 3:
    outfilename=sys.argv[3]
else:
    outfilename='table_unpivot.csv'
print("Using for output '%s'"%outfilename)
#%%
with open(inpfilename, 'r') as csvfile:
    dialect = csv.Sniffer().sniff(csvfile.read(10240))
    print("Detected csv input: separator:%s, quote:%s, eol:%s"%(repr(dialect.delimiter),repr(dialect.quotechar),repr(dialect.lineterminator)))
    csvfile.seek(0)
    reader = csv.reader(csvfile, dialect)
    inpdata=[row for row in reader]
#%%
valcolnums=[i+1 for i in range(len(inpdata[0])) if i+1 not in keycolnums]
keycolnames=[inpdata[0][i] for i in range(len(inpdata[0])) if i+1 in keycolnums]
valcolnames=[inpdata[0][i] for i in range(len(inpdata[0])) if i+1 in valcolnums]
print("Key Columns: %s"%','.join(keycolnames))
print("Key Columns: %s"%','.join(valcolnames))
#%%
#do the job
outdata=[keycolnames+["name","value"]]
for i in range(1,len(inpdata)):
    thisinpline=[]
    for v in valcolnums:
      if inpdata[i][v-1]:
        keys=[inpdata[i][k] for k in range(len(inpdata[i])) if k+1 in keycolnums]
        thisinpline.append(keys+[inpdata[0][v-1],inpdata[i][v-1]])
    outdata.extend(thisinpline)
print('Done. %d lines in output.'%(len(outdata)-1))
#%%
if not dialect.escapechar:
    dialect.escapechar='"'
with open(outfilename, 'w', newline='') as f:
    mywriter = csv.writer(f, dialect)
    mywriter.writerows(outdata)

