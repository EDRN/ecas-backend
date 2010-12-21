#!/usr/bin/perl

while(<>) {
   chomp;
   open FP, "$_" || die "cannot open: $0";
   
   while(<FP>) {
     next unless (/key/);
     next if (/keyval/);
     s/\s+$//;
     s/^\s+//;
     s/<[a-zA-Z\/][^>]*>//g;
     print $_ . "\n" ;
   }

   close FP;
}
