#!/usr/bin/perl

my $line = "";

while ($line = <>) {
   chomp($line);
   my @tokens = split(/[., !?]+/, $line);
   for (my $i = 0;$i < scalar(@tokens);$i++) {
       print $tokens[$i] . "\t" . "1" . "\n";
   }
}
