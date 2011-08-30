
DEL=".project .classpath .settings nb-configuration.xml nbactions.xml nbactions-large.xml"

for i in $DEL; do
    find . -name $i -exec rm -r {} \;
done

find . -name '*.iml' -exec rm -r {} \;
