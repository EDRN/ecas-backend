#!/usr/bin/perl

$site = "UTSW";
$productType = "UTSW_GeneExpressionRawDataZipped";

while(<>) {
   chomp;
   $outfile = $infile = $_;
   $outfile =~ s/met$/xml/;
   ($instrumentId,$filename) = split(/\//, $outfile);
   $productName = "$productType/$instrumentId/$filename";

   open FP, "$infile" || die "cannot open: $infile";
   open outFP, ">$outfile" || die "cannot open: $outfile";
   
   while(<FP>) {
     if ( /\<\/cas:metadata\>/ ) {
        print outFP qq(<keyval>
<key>SiteShortName</key>
<val>$site</val>
</keyval>
<keyval>
<key>InstrumentId</key>
<val>$productType-$instrumentId</val>
</keyval>
<keyval>
<key>ProductName</key>
<val>$productName</val>
</keyval>
);
        print outFP "</cas:metadata>\n";
     } 
     elsif (/SPECIMEN_AGE-COLLECTED_VALUE/) {
        print outFP "<key>SpecimenAgeCollectedValue</key>\n";
     }
     elsif (/SPECIMEN_COLLECTION_DATE/) {
        print outFP "<key>SpecimenCollectionDate</key>\n";
     }
     elsif (/SPECIMEN_FREEZE-THAW-VALUE/) {
        print outFP "<key>SpecimenFreezeThawValue</key>\n";
     }
     elsif (/SPECIMEN_PROCESS_TIMECOLLECTTOFINAL_CODE/) {
        print outFP "<key>SpecimenProcessTimeCollectionFinalCode</key>\n";
     }
     elsif (/SPECIMEN_SPECOLL_LABEL2_TEXT/) {
        print outFP "<key>SpecimenSpecollLabel2Text</key>\n";
     }
     elsif (/SPECIMEN_SPECOLL_SOURCE_TEXT/) {
        print outFP "<key>SpecimenSpecollSourceText</key>\n";
     }
     elsif (/SPECIMEN_STORED_CODE/) {
        print outFP "<key>SpecimenStoredCode</key>\n";
     }
     elsif (/SPECIMEN_STORED_CODE_TEXT/) {
        print outFP "<key>SpecimenStoredCodeText</key>\n";
     }
     elsif (/SPECIMEN_STUDY_PROTOCOL_CCDATACOMMENT_TEXT/) {
        print outFP "<key>SpecimenStudyProtocolCcDataCommentText</key>\n";
     }
     elsif (/SPECIMEN_STUDY_PROTOCOL_COMMENTS_TEXT/) {
        print outFP "<key>SpecimenStudyProtocolCommentsText</key>\n";
     } 
     elsif (/SPECIMEN_TISSUE_EGFRRESULT_CODE/) {
        print outFP "<key>SpecimenTissueEgfrResultCode</key>\n";
     }
     elsif (/SPECIMEN_TISSUE_EGFRRESULT_TEXT/) {
        print outFP "<key>SpecimenTissueEgfrResultText</key>\n";
     }
     elsif (/SPECIMEN_TISSUE_KRASRESULT_CODE/) {
        print outFP "<key>SpecimenTissueKrasResultCode</key>\n";
     }
     elsif (/SPECIMEN_TISSUE_KRASRESULT_TEXT/) {
        print outFP "<key>SpecimenTissueKrasResultText</key>\n";
     }
     elsif (/SPECIMEN_TISSUE_TUMORNORMAL_CODE/) {
        print outFP "<key>SpecimenTissueTumorNormalCode</key>\n";
     }
     elsif (/SPECIMEN_TISSUE_TUMORNORMAL_TEXT/) {
        print outFP "<key>SpecimenTissueTumorNormalText</key>\n";
     }
     elsif (/STUDY_PROTOCOL_SPECIMEN-ID-SITE_TEXT/) {
        print outFP "<key>StudyProtocolSpecimenIdSiteText</key>\n";
     }
     elsif (/STUDY_SITE_SPECIMENREC_ID/) {
        print outFP "<key>StudySiteSpecimenRecId</key>\n";
     } else {
        print outFP $_ ;
     }
   }

   close FP;
   close outFP;
   print "saving: [$outfile]\n";
}
