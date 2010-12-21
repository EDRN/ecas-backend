#!/bin/bash

for f in 1 2 3 4 5 ; do 
./filemgr-client --url http://localhost:9000 --operation --getFirstPage --productTypeName FHCRC_TEWARI_Controls | awk -F, '{ print $1 }' | awk -F= '{ print $2 }' > /tmp/productListing

for f in `cat /tmp/productListing`; do java -Djava.ext.dirs=../lib    gov.nasa.jpl.oodt.cas.filemgr.tools.DeleteProduct --fileManagerUrl http://localhost:9000 --productID $f ; done

done
