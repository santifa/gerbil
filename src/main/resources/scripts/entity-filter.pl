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


my @quoted_entities = map {quotemeta($_)} @entities;
my $regex = join " |", @quoted_entities;
#print "search items $regex\n";


my $length = @ARGV;
my @search_files = splice @ARGV, 1, $length;

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
          #print "rege before $regex\n";
          # reduce regex
          my $found = (split / /, $line)[0];
          my $quoted_found = quotemeta $found;
          #print "found is $found and $quoted_found\n";

          my @result = grep ! /$quoted_found/, @entities;
          #print "result is @result\n";
          @quoted_entities = map {quotemeta} @result;
          #print "quoted is @quoted_entities\n";

          $regex = join " |", @quoted_entities;
          #print  "regex is $regex\n";
          print $found, "\n";
          }
        }
      close $f;

      #my @result = grep (/^$regex/, <$f>);
      #if (@result) {
      #  print @result;
      #} else {
      #  exit 1;
      #}

    #close $f;
  }
}

exit 0;
