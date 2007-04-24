#! /bin/sh


# add the libraries to the SPECIFY_CLASSPATH.
# EXEDIR is the directory where this executable is.
EXEDIR=${0%/*}
DIRLIBS=${EXEDIR}/Specify/libs/*.jar
for i in ${DIRLIBS}
do
  if [ -z "$SPECIFY_CLASSPATH" ] ; then
    SPECIFY_CLASSPATH=$i
  else
    SPECIFY_CLASSPATH="$i":$SPECIFY_CLASSPATH
  fi
done

#DIRLIBS=${EXEDIR}/Specify/lib/*.zip
#for i in ${DIRLIBS}
#do
#  if [ -z "$SPECIFY_CLASSPATH" ] ; then
#    SPECIFY_CLASSPATH=$i
#  else
#    SPECIFY_CLASSPATH="$i":$SPECIFY_CLASSPATH
#  fi
#done

SPECIFY_CLASSPATH="${EXEDIR}/Specify/classes":$SPECIFY_CLASSPATH:"${EXEDIR}/Specify/help"
SPECIFY_HOME=$(pwd)

echo $SPECIFY_HOME
#echo $SPECIFY_CLASSPATH

JAVA_HOME=${EXEDIR}/jre

java -classpath "$SPECIFY_CLASSPATH:$CLASSPATH" -Dappdir=$SPECIFY_HOME/Specify -Dappdatadir=$SPECIFY_HOME -Djavadbdir=$SPECIFY_HOME/DerbyDatabases edu.ku.brc.specify.Specify "$@"
