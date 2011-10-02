#!/bin/bash
grep -rl org\.andglk\. . | xargs perl -pi~ -e "s/org\.andglk\./org\.andglkmod\./"
grep -rl org\/andglk\/ . | xargs perl -pi~ -e "s/org\/andglk\//org\/andglkmod\//"
rm -rf src/org/andglkmod
mv src/org/andglk src/org/andglkmod