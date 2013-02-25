#!/bin/bash
DIR="./lib"
 
OLDIFS=$IFS
IFS=$'\n'
 
fileArray=($(find $DIR -name *.jar -type f))
 
IFS=$OLDIFS
 
CLASS_PATH=$DIR
tLen=${#fileArray[@]}
for (( i=0; i<${tLen}; i++ ));
do
  CLASS_PATH=$CLASS_PATH:${fileArray[$i]}
done
export CLASS_PATH
java -cp $CLASS_PATH com.github.vmorev.crawler.tools.Hoster
