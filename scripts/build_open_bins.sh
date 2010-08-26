#!/bin/zsh

## Copy all bin files but the Private package

BINPATH=../nand/bin
BUILDROOT=../build

TARGETBINPATH=$BUILDROOT/bin
CHIPSOURCE=$BINPATH/builtInChips
VMSOURCE=$BINPATH/builtInVMCode
CHIPSDIR=$BUILDROOT/builtInChips
VMDIR=$BUILDROOT/builtInVMCode

echo "cleaning up"
find $BUILDROOT -name "*.class" | xargs rm -f 
find $BUILDROOT -name "*.jar" | xargs rm -f

for package in common simulators translators; do
    echo "Copying package ${package}"
    cp -Rf $BINPATH/$package $TARGETBINPATH
done

echo "Packing private files which should be included"
cd $TARGETBINPATH
###### Any private files that should be included should be handled here
jar -cMf lib/translators.jar translators/syntaxAnalyzer/Private
#######################################################################
cd -

echo "removing private files"
find $TARGETBINPATH -name "*Private*" | xargs rm -rf

echo "Handling built in chips"
cp $CHIPSOURCE/*.class $CHIPSDIR

echo "Handling built in VM code"
cp $VMSOURCE/*.class $VMDIR

cd $TARGETBINPATH
for package in common simulators translators; do
    echo "Packaging package $package"
    [[ -a lib/$package.jar ]] && jar -uMf lib/$package.jar $package || jar -cMf lib/$package.jar $package
    rm -rf $package
done