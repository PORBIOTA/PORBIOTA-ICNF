CONVERT="convert"
EXIFTOOL="exiftool"
TESSERACT="tesseract -l eng+por --psm 1 --oem 1 -c tessedit_create_hocr=1"
HOCRTRANSFORM="hocrtransform.py"
#SRCDIR="/data/FotoRestauro"
SRCDIR="$1"
BASESRCDIR="${SRCDIR##*/}"
TEMPDIR="$XDG_RUNTIME_DIR"
DESTDIR="$TEMPDIR"/"$BASESRCDIR"
mkdir "$DESTDIR"
#read
FILESJPG=()
FILESHOCR=()
inps=($(find "$SRCDIR" -name \*.tif | sort | tail --lines=+2 | head -n -1))
for L in "${inps[@]}"
do 
  BASENAME="${L##*/}"
  EXT="${BASENAME##*.}"
  NAME="${BASENAME%.*}"
  DIR="${L%/*}"
  #DOSDIR="${DIR//\//\\}"
  TEMPJPG="$TEMPDIR"/"$NAME".jpg
  SRCTIFF="$L" # "$SRCDIR"/"$L"
  echo -n "$SRCTIFF -> $DESTDIR/$NAME.hocr; "
  $TESSERACT "$SRCTIFF" "$DESTDIR/$NAME"
  FILESHOCR+=("${DESTDIR}"/"${NAME}.hocr")
  echo -n "$SRCTIFF -> $TEMPJPG"
  "$CONVERT" "$SRCTIFF" -level "27%,73%" -quality 86 -interlace Plane "$TEMPJPG"
  echo -n " -> $DESTDIR/$NAME.jpg" 
  "$EXIFTOOL" -tagsFromFile "$SRCTIFF" -EXIF:ALL -XMP:ALL "$TEMPJPG" -PhotoshopThumbnail">"ThumbnailImage -o "$DESTDIR"/"$NAME.jpg"
  touch -r "$SRCTIFF" -c "$DESTDIR"/"$NAME.jpg"
  FILESJPG+=("${DESTDIR}"/"${NAME}.jpg")
  rm "$TEMPJPG"
  #exit
  #echo copy /b \""$SRCDIRDOS"\\"$DOSPATH"\" _"$BASENAME"_ "$TEMPTIF"
done
python3 "$HOCRTRANSFORM" -f 'Helvetica' -o "$BASESRCDIR".pdf "${FILESHOCR[@]}"
rm -v "${FILESJPG[@]}"
rm -v "${FILESHOCR[@]}"
rmdir -v "$DESTDIR"
