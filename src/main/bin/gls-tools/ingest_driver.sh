## ./crawler_launcher -cid StdProductCrawler -pp /data/ingest/canary/FHCRC_TEWARIControls -fm http://localhost:9000 -ct gov.nasa.jpl.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory -mfx xml 

## ./crawler_launcher -cid StdProductCrawler -pp /data/ingest/canary/FHCRC_TEWARIEfficiencies -fm http://localhost:9000 -ct gov.nasa.jpl.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory -mfx xml 

## ./crawler_launcher -cid StdProductCrawler -pp /data/ingest/canary/FHCRC_TEWARIRaw_Cp_All -fm http://localhost:9000 -ct gov.nasa.jpl.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory -mfx xml 

##./crawler_launcher -cid StdProductCrawler -pp /data/ingest/canary/FHCRC_TEWARIRaw_Cp_below_NoEnzRT -fm http://localhost:9000 -ct gov.nasa.jpl.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory -mfx xml 

./crawler_launcher -cid StdProductCrawler -pp /data/ingest/canary/UTSW_CopyNumberData -fm http://localhost:9000 -ct gov.nasa.jpl.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory -mfx xml 

./crawler_launcher -cid StdProductCrawler -pp /data/ingest/canary/UTSW_MutationData -fm http://localhost:9000 -ct gov.nasa.jpl.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory -mfx xml 

./crawler_launcher -cid StdProductCrawler -pp /data/ingest/canary/UTSW_GeneExpressionAnalyzedData -fm http://localhost:9000 -ct gov.nasa.jpl.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory -mfx xml 

./crawler_launcher -cid StdProductCrawler -pp /data/ingest/canary/UTSW_GeneExpressionRawDataZipped/ -fm http://localhost:9000 -ct gov.nasa.jpl.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory -mfx xml 
