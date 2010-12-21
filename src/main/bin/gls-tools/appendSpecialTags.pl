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
     } else {
        print outFP $_ ;
     }
   }

   close FP;
   close outFP;
   print "saving: [$outfile]\n";
}
