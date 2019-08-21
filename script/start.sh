#! /bin/bash
SOURCE_FOLDER=/Users/liebes/project/laboratory/Sip4J

function func(){
    for file in `cat benchmark.txt`
    do
#        echo "shell : start exec folder :" ${file}
#        echo ".............start........."
#        echo "java -jar "${SOURCE_FOLDER}"/target/Sip4J-1.0-SNAPSHOT-jar-with-dependencies.jar" ${file}
#        echo `pwd`"/"${file}
        java -jar ${SOURCE_FOLDER}"/"target/Sip4J-1.0-SNAPSHOT-jar-with-dependencies.jar ${file} $1
#        echo ".............end..........."
    done
}

func $1 $2