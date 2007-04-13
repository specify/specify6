#! /bin/sh


# add the libraries to the SPECIFY_CLASSPATH.
# EXEDIR is the directory where this executable is.
EXEDIR=${0%/*}
DIRLIBS=${EXEDIR}/libs/*.jar
for i in ${DIRLIBS}
do
  if [ -z "$SPECIFY_CLASSPATH" ] ; then
    SPECIFY_CLASSPATH=$i
  else
    SPECIFY_CLASSPATH="$i":$SPECIFY_CLASSPATH
  fi
done

#DIRLIBS=${EXEDIR}/lib/*.zip
#for i in ${DIRLIBS}
#do
#  if [ -z "$SPECIFY_CLASSPATH" ] ; then
#    SPECIFY_CLASSPATH=$i
#  else
#    SPECIFY_CLASSPATH="$i":$SPECIFY_CLASSPATH
#  fi
#done

SPECIFY_CLASSPATH="${EXEDIR}/classes":$SPECIFY_CLASSPATH:"${EXEDIR}/help"
#cd ..
SPECIFY_HOME=$(pwd)
#cd bin

echo $SPECIFY_HOME
echo $SPECIFY_CLASSPATH

JAVA_HOME=${EXEDIR}/jre

java -classpath "$SPECIFY_CLASSPATH:$CLASSPATH" -Dspecify.home=$SPECIFY_HOME edu.ku.brc.specify.Specify "$@"
