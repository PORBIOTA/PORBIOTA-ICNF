#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys,os
import csv
import uuid

#%%
if len(sys.argv) > 1:
    inpfilename=sys.argv[1]
else:
    inpfilename='ocupdata.csv'

basename=os.path.splitext(os.path.basename(inpfilename))[0]

if len(sys.argv) > 2:
    outfilename=sys.argv[2]
else:
    outfilename='%s_dup.csv'%basename
print("Using for output '%s'"%outfilename)

if len(sys.argv) > 3:
    relfilename=sys.argv[3]
else:
    relfilename='%s_rel.csv'%basename
print("Using for resourcerelationship '%s'"%relfilename)

if os.path.exists(outfilename):
    print("output file '%s' exists. Quitting."%outfilename)
    sys.exit()

if os.path.exists(relfilename):
    print("resourcerelationship file '%s' exists. Quitting."%relfilename)
    sys.exit()

#%%
with open(inpfilename, 'r') as csvfile:
    dialect = csv.Sniffer().sniff(csvfile.read(10240))
    print("Detected csv input: separator:%s, quote:%s, eol:%s"%(repr(dialect.delimiter),repr(dialect.quotechar),repr(dialect.lineterminator)))
    csvfile.seek(0)
    reader = csv.reader(csvfile, dialect)
    inpdata=[row for row in reader]
#%%
colidx=False
colpri=False
colsec=False
coloID=False
corcount=0
outdata=[]
outrow=[]
for i in range(len(inpdata[0])):
    if 'idx'==inpdata[0][i]:
        colidx=i
        outrow.append('idx')
    elif 'ocupprinci'==inpdata[0][i]:
        colpri=i
        outrow.append('ocup')
    elif 'ocupsecund'==inpdata[0][i]:
        colsec=i
    elif 'occurrenceID'==inpdata[0][i]:
        coloID=i
        outrow.append('occurrenceID')
    else:
        outrow.append(inpdata[0][i])
if False==coloID:
    outrow.append('occurrenceID') #add column UUID if not exists
outdata.append(outrow)
#print("Columns: Idx:%s, ocupprinci:%s, ocupsecund:%s, occurrenceID:%s"%(str(colidx),str(colpri),str(colsec),str(coloID),))
#%%
if colpri==False or colsec==False:
    print("Column \"ocupprinci\" or \"ocupsecund\" not found. EOS.")
    sys.exit()
#%%
rels=[]
rels.append(['idx','resourceID','relatedResourceID','relationshipOfResource'])
#print(outdata)
#sys.exit()
#%%
rc=0
for ri in range(1,len(inpdata)-1):
  r=inpdata[ri]
  outrow=[]
  outrowsec=[]
  hassec=r[colsec] and r[colsec].strip()
  haspri=r[colpri] and r[colpri].strip()
  if haspri and hassec and (r[colpri].strip()==r[colsec].strip()):
      hassec=False # we shal not repeat lines where ocuppri and ocupsec are equal
  for i in range(len(inpdata[0])):
    if colidx is not False and i==colidx:
        outrow.append('%s_p'%str(r[i]))          
        outrowsec.append('%s_s'%str(r[i]))          
    elif i==colpri:
        outrow.append(r[i])
    elif i==colsec:
        outrowsec.append(r[i])
    elif coloID is not False and i==coloID:
        if r[i] and r[i].strip():
            #there is uuid, assign to pri if has pri or to sec otherwise
            if haspri:
                outrow.append(r[i])
                outrowsec.append(str(uuid.uuid4())) #new uuid for sec
            else:
                outrow.append(r[i]) 
                outrowsec.append(r[i]) #dummy value, just to keep length consistent
        else:
            #there is no UUID - get new uuids for both
            outrow.append(str(uuid.uuid4()))
            outrowsec.append(str(uuid.uuid4()))
    else:
        outrow.append(r[i])
        outrowsec.append(r[i])
  if coloID is False:
    outrow.append(str(uuid.uuid4()))
    outrowsec.append(str(uuid.uuid4()))
  if haspri and hassec:
    lenrow=len(outdata)
    outdata.append(outrow)
    outdata.append(outrowsec)
    rc+=1
    refrow=[rc,outrow[coloID if coloID is not False else outdata[lenrow-1]],
            outrowsec[coloID if coloID is not False else outdata[lenrow-1]],
            'is primary of']
    rels.append(refrow)        
    rc+=1
    refrow=[rc,outrowsec[coloID if coloID is not False else outdata[lenrow-1]],
            outrow[coloID if coloID is not False else outdata[lenrow-1]],
            'is secondary of']
    rels.append(refrow)  
  elif haspri and not hassec:
    outdata.append(outrow)
  elif not haspri and hassec:
    outdata.append(outrowsec)
  elif not (haspri or hassec):
    outdata.append(outrow) #output primary even with empty ocuppri
#%%
print('Done. %d lines processed, %d output lines;'%(len(inpdata)-1,len(outdata)-1))
with open(outfilename, 'w', newline='') as f:
    mywriter = csv.writer(f, dialect)
    mywriter.writerows(outdata)
#%%
print('%d relations.'%(len(rels)-1))
with open(relfilename, 'w', newline='') as f:
    mywriter = csv.writer(f, dialect)
    mywriter.writerows(rels)
