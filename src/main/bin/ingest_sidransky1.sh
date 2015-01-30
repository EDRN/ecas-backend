cd /usr/local/ecas/filemgr/bin
./filemgr-client --url http://localhost:9000 --operation \
   --ingestProduct --productName methylation_data_2.sas7bdat --productStructure Flat \
   --productTypeName GSTP1_Methylation \
   --metadataFile file:///data/ingest/sidransky/methylation_data_2.sas7bdat.met \
   --refs file:///data/ingest/sidransky/methylation_data_2.sas7bdat
