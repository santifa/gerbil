#!/usr/bin/perl -w
#
# This script creates the metadata from the cached dataset goldstandard.
# The metadata contains information about the amounts of entities per filter
#
use strict;
use warnings;
use autodie;
use open qw< :encoding(UTF-8)>;

my $basedir = '../../../../gerbil_data';

# get file handles in and out
opendir(my $cache_dir, "$basedir/cache/filter") or die $!;
my @files = grep(/.*_gt_.*/, readdir($cache_dir));
closedir $cache_dir;

open(my $result_file, ">$basedir/resources/filter/metadata") or die $!;

# iterate over all cached ground truth
foreach my $file (@files) {
  open my $f, '<', "$basedir/cache/filter/$file";
  chomp(my @lines = <$f>); # get all lines
  my $uris = grep(/.*http.*/, @lines);
  my ($filter, $dataset) = split /_gt_/, $file; # get filtername and dataset name
  print $result_file "$filter $dataset $uris \n"; 
  close $f;
}

close $result_file;
