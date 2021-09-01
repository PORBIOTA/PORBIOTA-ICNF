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
    outfilename='%s_evt.csv'%basename
print("Using for event '%s'"%outfilename)

if len(sys.argv) > 3:
    occfilename=sys.argv[3]
else:
    occfilename='%s_occ.csv'%basename
print("Using for occurence '%s'"%occfilename)

if len(sys.argv) > 4:
    relfilename=sys.argv[4]
else:
    relfilename='%s_rel.csv'%basename
print("Using for resourcerelationship '%s'"%relfilename)

if os.path.exists(outfilename):
    print("event file '%s' exists. Quitting."%outfilename)
    sys.exit()

if os.path.exists(occfilename):
    print("occurence file '%s' exists. Quitting."%occfilename)
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
copyToOccurence=('basisOfRecord',
  'license','institutionCode','institutionID',
  'collectionCode','datasetName',
  'decimalLatitude','decimalLongitude',
  'country','countryCode','stateProvince','municipality','locality')
#%%
colidx=False
colpri=False
colsec=False
coloccID=False
coloccOccurenceID=False
colevID=False
corcount=0
evts=[]
occs=[]
outrow=[]
outoccrow=['idx','type','eventID']
for i in range(len(inpdata[0])):
    if 'idx'==inpdata[0][i]:
        colidx=i
        outrow.append('idx')
    elif 'ocupprinci'==inpdata[0][i]:
        colpri=i
        outoccrow.append('ocup')
    elif 'ocupsecund'==inpdata[0][i]:
        colsec=i
    elif 'occurrenceID'==inpdata[0][i]:
        coloccID=i
        coloccOccurenceID=len(outoccrow)
        outoccrow.append('occurrenceID')
        #outrow.append('occurrenceID') occurenceID will go to occurence file and relationships
    elif 'eventID'==inpdata[0][i]:
        colevID=i
        outrow.append('eventID')
    else:
        outrow.append(inpdata[0][i])
    if inpdata[0][i] in copyToOccurence:
        outoccrow.append(inpdata[0][i])
if colevID is False:
    outrow.append('eventID') #add column UUID at the if not exists
if coloccID is False:
    coloccOccurenceID=len(outoccrow)
    outoccrow.append('occurrenceID') #add column UUID at the if not exists
evts.append(outrow)
occs.append(outoccrow)

#print("Columns: Idx:%s, ocupprinci:%s, ocupsecund:%s, occurrenceID:%s"%(str(colidx),str(colpri),str(colsec),str(coloID),))
#%%
if colpri==False or colsec==False:
    print("Column \"ocupprinci\" or \"ocupsecund\" not found. EOS.")
    sys.exit()
#%%
rels=[]
rels.append(['idx','eventID','resourceID','relatedResourceID','relationshipOfResource'])
#print(evts)
#print(occs)
#print(rels)
#sys.exit()
#%%
for ri in range(1,len(inpdata)):
  r=inpdata[ri]
  outrow=[]
  outoccpri=[]
  outoccsec=[]
  hassec=r[colsec] and r[colsec].strip()
  haspri=r[colpri] and r[colpri].strip()
  if haspri and hassec and (r[colpri].strip()==r[colsec].strip()):
      hassec=False # we shal not repeat lines where ocuppri and ocupsec are equal
  occidxbase=str(ri) if colidx is False else r[colidx]
  outoccpri.append('%s_com'%occidxbase if hassec is False else '%s_pri'%occidxbase)
  outoccsec.append('%s_sec'%occidxbase) #will be discarded if not hassec anyways
  #create eventID UUID if column does not exist or empty otherwize copy
  outoccpri.append('Event')
  outoccsec.append('Event')
  evtid=str(uuid.uuid4()) if colevID is False else (str(uuid.uuid4()) if r[colevID] and r[colevID].strip() else str(uuid.uuid4()))
  outoccpri.append(evtid)
  outoccsec.append(evtid)
  for i in range(len(inpdata[0])):
    if i==colpri:
        outoccpri.append(r[i])
    elif i==colsec:
        outoccsec.append(r[i])
    elif coloccID is not False and i==coloccID:
        if r[i] and r[i].strip():
            #there is uuid, assign to pri if has pri or to sec otherwise
            if haspri:
                outoccpri.append(r[i])
                outoccsec.append(str(uuid.uuid4())) #new uuid for sec
            else:
                outoccpri.append(r[i]) 
                outoccsec.append(r[i]) #dummy value, just to keep length consistent
        else:
            #there is no UUID - get new uuids for both
            outoccpri.append(str(uuid.uuid4()))
            outoccsec.append(str(uuid.uuid4()))
    else:
        outrow.append(r[i])
    if inpdata[0][i] in copyToOccurence:
        outoccpri.append(r[i]) 
        outoccsec.append(r[i])
  if colevID is False:
    outrow.append(evtid)
  if coloccID is False: #there was no occurenceID column in the input. Append new UUID for both
    outoccpri.append(str(uuid.uuid4()))
    outoccsec.append(str(uuid.uuid4()))
  evts.append(outrow)
  if haspri and hassec:
    occs.append(outoccpri)
    occs.append(outoccsec)
    refrow=[outoccpri[0],evtid,
            outoccpri[coloccOccurenceID],
            outoccsec[coloccOccurenceID],
            'is primary of']
    rels.append(refrow)        
    refrow=[outoccsec[0],evtid,
            outoccsec[coloccOccurenceID],
            outoccpri[coloccOccurenceID],
            'is secondary of']
    rels.append(refrow)  
  elif haspri and not hassec:
    occs.append(outoccpri)
  elif not haspri and hassec:
    occs.append(outoccsec)
  elif not (haspri or hassec):
    occs.append(outoccpri) #output primary even with empty ocuppri
#%%
print('Done. %d lines processed, %d event lines;'%(len(inpdata)-1,len(evts)-1))
with open(outfilename, 'w', newline='') as f:
    mywriter = csv.writer(f, dialect)
    mywriter.writerows(evts)
print('%d occurence lines;'%(len(occs)-1))
with open(occfilename, 'w', newline='') as f:
    mywriter = csv.writer(f, dialect)
    mywriter.writerows(occs)
#%%
print('%d relations.'%(len(rels)-1))
with open(relfilename, 'w', newline='') as f:
    mywriter = csv.writer(f, dialect)
    mywriter.writerows(rels)
