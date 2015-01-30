cd /usr/local/ecas/filemgr/bin
./filemgr-client --url http://localhost:9000 --operation \
   --ingestProduct --productName "Methylation%20Data%20Codebook(2).docx" --productStructure Flat \
   --productTypeName GSTP1_Methylation \
   --metadataFile "file:///data/ingest/sidransky/Methylation%20Data%20Codebook(2).docx.met" \
   --refs "file:///data/ingest/sidransky/Methylation%20Data%20Codebook(2).docx"
