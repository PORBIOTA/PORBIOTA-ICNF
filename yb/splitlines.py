import re
import sys,os
import random

itemsre=re.compile(r'(?:[^\s,"\']|\'(?:\\.|[^\'])*\'|"(?:\\.|[^"])*")+[\s,]*')
isSDO_ORDINATE_ARRAYstart=re.compile(r'^\s*(MDSYS\.)?SDO_ORDINATE_ARRAY')
isEndArguments=re.compile(r'^\s*([+-])?[0-9\.]*\)')
re2Floats=re.compile(r'\s*([+-]?\d*\.?\d*),\s*([+-]?\d*\.?\d*),?')
re3Ints=re.compile(r'\s*([+-]?\d+),\s*([+-]?\d+),\s*([+-]?\d+),?')
#%%
longparammatch=re.compile(r'(MDSYS\.)?(SDO_[A-Z_]+_ARRAY)\s*\(((\s*[+-]?[\d.]+,?)*)\s*\)')
random.seed(2)
maxfuncparams=999
#longparammatch.sub(limit_arguments,line)
#%%
def limit_arguments(m):
    if m.group(2)=='SDO_ORDINATE_ARRAY':
        maxc=int(maxfuncparams/2)
        out=[','.join(t) for t in re2Floats.findall(m.group(3))]
#        if out[0]!=out[len(out)-1]:
#            print("copy first to last")
#            out.append(out[0])
    elif m.group(2)=='SDO_ELEM_INFO_ARRAY':
        maxc=int(maxfuncparams/3)
        out=[','.join(t) for t in re3Ints.findall(m.group(3))]
    else:
        maxc=maxfuncparams
        out=itemsre.findall(m.group(3))
    while len(out)>maxc:
        out.pop(random.randint(1,len(out)-2))
    return m.group(2)+'('+','.join(out)+')'
    
#%%
if len(sys.argv) > 1:
    inpfilename=sys.argv[1]
else:
    inpfilename='input.sql'
print("Using for input '%s'"%inpfilename)
#%%
basename=os.path.splitext(os.path.basename(inpfilename))[0]

if len(sys.argv) > 2:
    outfilename=sys.argv[2]
else:
    outfilename='%s.out.sql'%basename
print("Using for ouput '%s'"%outfilename)
#%%
if os.path.exists(outfilename):
    print("output file '%s' exists. Quitting."%outfilename)
    sys.exit()

#%%
fout=open(outfilename, 'a')
with open(inpfilename) as linesfile: 
    for origline in linesfile:
      shorterline=longparammatch.sub(limit_arguments,origline.rstrip('\r\n'))
      linelen=len(shorterline)
      if False and len(shorterline) > 4000:
        fields=itemsre.findall(shorterline)  
        #fields=shlex.split(line,posix=True)
        newlines=[]
        newlineslens=[]
        thisline=''
        curlen=0
        cfields=len(fields)
        for i in range(cfields):
            fieldlen=len(fields[i])
            if curlen==0:
                thisline=fields[i]
                curlen=fieldlen
            elif curlen+fieldlen>4000:
                newlines.append(thisline)
                newlineslens.append(curlen)
                thisline=fields[i]
                curlen=fieldlen
            else:
                thisline+=fields[i]
                curlen+=fieldlen
        newlines.append(thisline)
        newlineslens.append(curlen)
      else:
        newlines=[shorterline]
        newlineslens=[linelen]
      print("length: % 8d % 4d % 8d %s"%(len(origline),len(newlines),sum(newlineslens),','.join([str(n) for n in newlineslens])))
#      [fout.write(l+('' if l[-1]=='\n' else '\n')) for l in newlines]
      [fout.write(l+'\n') for l in newlines]
fout.close()