#!/usr/bin/perl -w
#
# Entity Filter
# This script searches for entities in different files.
# To do so it takes as first argument a file containing an entity every line
# and as other arguments the files for searching.
# A search file should contain at first column the entity.
# As result it prints to standard out all found entities one line each.
#
# Note encoding is ignored due to many failueres in the provided files.
#
use strict;
use warnings;
use autodie;

# get search entities
open my $regex_file, '<', $ARGV[0] or die "File containing entities not found $ARGV[1]: $!";
chomp(my @entities = <$regex_file>);
close $regex_file;

# quote non ASCII chars and get search files
my @quoted_entities = map {quotemeta} @entities;
my $regex = join " |", @quoted_entities;

#my $length = @ARGV;
my @search_files = splice @ARGV, 1, @ARGV;

foreach my $file (@search_files) {
  if (open my $f, '<', $file) {
      if ($regex eq "") {
        last;
      }

      while(my $line = <$f>) {
        if ($regex eq "") {
          last;
        }

        if ($line =~ /^($regex)/) {
          # reduce regex
          my $found = (split / /, $line)[0];
          my $quoted_found = quotemeta $found;
          my @result = grep ! /$quoted_found/, @entities;

          @quoted_entities = map {quotemeta} @result;
          $regex = join " |", @quoted_entities;
          print $found, "\n";
          }
        }
      close $f;
  }
}

exit 0;
