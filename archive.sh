#!/bin/bash

make clean

TEMPDIR="$$-vma-archive"

cd ..
mkdir $TEMPDIR
cp -Rv VMA $TEMPDIR
cd $TEMPDIR

mv VMA/0-Refs .

tar -cjvpf VMA-Prog-`date +%Y%m%d-%H%M`.tar.bz2 VMA
tar -cjvpf VMA-Refs-`date +%Y%m%d-%H%M`.tar.bz2 0-Refs
mv *.tar.bz2 ..

cd ..
rm -rvf $TEMPDIR
