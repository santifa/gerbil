#!/usr/bin/perl -w
#
use strict;
use warnings;
use autodie;
#use open qw< :encoding(UTF-8)>;

# get search entitiess
open my $regex_file, '<', $ARGV[0] or die "File containing entities not found $ARGV[1]: $!";
chomp(my @entities = <$regex_file>);
close $regex_file;

my $regex = "(";
$regex .= join " |", map {$_ => quotemeta($_)} @entities;
$regex .= " )";

my $length = @ARGV;
my @search_files = splice @ARGV, 1, $length;

foreach my $file (@search_files) {
  if (open my $f, '<', $file) {
    my @result = grep (/^$regex/, <$f>);
    if (@result) {
       print @result;
    } else {
      exit 1;
    }

    close $f;
  }
}

exit 0;
