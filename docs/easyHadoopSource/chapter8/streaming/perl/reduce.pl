#!/usr/bin/perl

my $line = "";
my $sum = 0;
my $prev_key = "";

while ($line = <>) {
   chomp($line);

   my @tokens = split(/\t/, $line);
   my $key = $tokens[0];
   my $value = $tokens[1];

   if ($key eq $prev_key) {
       $sum += $value;
   }
   else {
       if ($prev_key ne "") {
           print $prev_key . "\t" . $sum . "\n";  
       }
       $sum = $value;
       $prev_key = $key;
   }
}
print $prev_key . "\t" . $sum . "\n";

