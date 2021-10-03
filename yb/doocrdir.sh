#!/bin/bash
CONVERT="convert"
EXIFTOOL="exiftool"
TESSERACT="tesseract -l eng+por --psm 1 --oem 1 -c tessedit_create_hocr=1 -c debug_file=/dev/null"
HOCRTRANSFORM="hocrtransform.py"
TEMPDIR="${XDG_RUNTIME_DIR:-/tmp}"

LEVEL="${2:-41%,76%}"
SRCDIR=$(realpath --no-symlinks --quiet --canonicalize-existing "${1}") || { echo 1>&2 "$1 does not exist"; exit 1; } # "${1%%/}"
BASESRCDIR="${SRCDIR##*/}"
[ x"$BASESRCDIR"x == 'xx' ] && { echo 1>&2 'base directory name too short'; exit 1; }
DESTDIR="$TEMPDIR"/"$$"-"$BASESRCDIR"
mkdir "$DESTDIR"
FILESJPG=()
FILESHOCR=()
inps=($(find "$SRCDIR" -iname \*.tif | sort ))
#inps=("${inps[@]:1}") #skip first
unset 'inps[${#inps[@]}-1]' #skip last
unset 'inps[0]' #skip first
for L in "${inps[@]}"
do 
  BASENAME="${L##*/}"
  EXT="${BASENAME##*.}"
  NAME="${BASENAME%.*}"
  DIR="${L%/*}"
  #DOSDIR="${DIR//\//\\}"
  TEMPJPG="$TEMPDIR"/"$$"-"$NAME".jpg
  SRCTIFF="$L" # "$SRCDIR"/"$L"
  echo -n "$SRCTIFF -> $DESTDIR/$NAME.hocr; "
  $TESSERACT "$SRCTIFF" "$DESTDIR/$NAME"
  FILESHOCR+=("${DESTDIR}"/"${NAME}.hocr")
  echo -n "$SRCTIFF -> $TEMPJPG"
  "$CONVERT" "$SRCTIFF" -level "$LEVEL" -quality 86 -interlace Plane "$TEMPJPG"
  echo -n " -> $DESTDIR/$NAME.jpg" 
  "$EXIFTOOL" -tagsFromFile "$SRCTIFF" -EXIF:ALL -XMP:ALL "$TEMPJPG" -o "$DESTDIR"/"$NAME.jpg"
  touch -r "$SRCTIFF" -c "$DESTDIR"/"$NAME.jpg"
  FILESJPG+=("${DESTDIR}"/"${NAME}.jpg")
  rm "$TEMPJPG"
done
python3 "$HOCRTRANSFORM" -f 'Helvetica' -o "$BASESRCDIR".pdf "${FILESHOCR[@]}"
rm -v "${FILESJPG[@]}"
rm -v "${FILESHOCR[@]}"
rmdir -v "$DESTDIR"
