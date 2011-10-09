#!/bin/bash
grep -rl org\.andglk\. . | xargs perl -pi~ -e "s/org\.andglk\./org\.andglkmod\./g"
grep -rl org\/andglk\/ . | xargs perl -pi~ -e "s/org\/andglk\//org\/andglkmod\//g"
rm -rf src/org/andglkmod
mv src/org/andglk src/org/andglkmod