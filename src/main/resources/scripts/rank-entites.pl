#!/usr/bin/perl -w
#
# This script creates the files used for popularity filtering.
# It takes all entities stored in the datasets and ranks them using the 
# score files in /gerbil_data/resources/filter/source . These files should be
# in trippel format. It works offline and should be done before you run gerbil.
#
#
use strict;
use warnings;
use autodie;
use File::Find;
use RDF::RDFa::Parser;
#use open qw< :encoding(UTF-8)>; datasets have crazy encodings

# get all dataset files
my $basedir = '../../../../gerbil_data';
my @filenames;
find({wanted => sub {push @filenames, $File::Find::name if -f},}, "$basedir/datasets");

# convert all rdf files to n3 notation
foreach my $file (@filenames) {
  open my $f, '<', $file;
  chomp(my @lines =  <$f>);

  my %prefixes = ();
  foreach my $line (@lines) {
    if ($line =~ /^\@?[pP][rR][eE][fF][iI][xX]/) {
      my @parts = split / /, $line;
      $prefixes{$parts[1]} = $parts[2]; 
    }
  }

  print $file;
  while (my ($key, $value) = each(%prefixes)) {
    print "$key => $value\n";
  }
}

