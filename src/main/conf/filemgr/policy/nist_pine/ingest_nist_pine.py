#!/usr/bin/python

from os import listdir, system
from os.path import isfile, join

DIRECTORY = "/data/ingest/nist_pine"
COMMAND = ["cd /usr/local/ecas/filemgr/bin;",
           "./filemgr-client",
           "--url http://localhost:9000",
           "--operation",
           "--ingestProduct",
           "--productName FILENAME",
           "--productStructure Flat",
           "--productTypeName miRNAStudyPine",
           "--metadataFile file:///data/ingest/nist_pine/FILENAME.xml",
           "--refs file:///data/ingest/nist_pine/FILENAME"]  

# loop over data files in directory
files = [ f for f in listdir(DIRECTORY) if isfile(join(DIRECTORY,f)) ]
for file in files:
    # exclude the metadata files
    if not file.endswith(".xml"): 

        # invoke filengr-client to ingest product
        command = " ".join(COMMAND).replace("FILENAME", file)
        print command
        system(command)
