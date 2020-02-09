#!/usr/bin/env bash

JAR_NAME="httpc-jar-with-dependencies.jar"
FINAL_NAME="httpc"
BUILD_DIR="target"

FINAL_PATH="$BUILD_DIR/$FINAL_NAME"
JAR_PATH="$BUILD_DIR/$JAR_NAME"

echo "#!/usr/bin/java -jar" > $FINAL_PATH
cat $JAR_PATH >> $FINAL_PATH
chmod +x $FINAL_PATH