#!/bin/sh

for f in `\ls | grep -v foo`; do
  echo $f
  mv $f/$f/* $f/
  rmdir $f/$f
done 
