#! /bin/bash

function read_dir(){
#echo $1
    local flag=1
#    if [[ -f $1 ]]
#    then
#        return 0
#    fi
#    echo "reading folder : "$1
    for file in `ls $1`       #注意此处这是两个反引号，表示运行系统命令
    do
#        echo $1"/"${file}
        if [[ -d $1"/"${file} ]]  #注意此处之间一定要加上空格，否则会报错
        then
            flag=2
#            echo $1"/"${flag}
            read_dir $1"/"${file}
        fi
    done
    if [[ ${flag} -eq 1 ]]
    then
        echo $1
    fi
#    echo "leave folder :"$1
}
#读取第一个参数

function func(){
    for file in `read_dir $1`
    do
        echo "shell : start exec folder :" ${file}
        echo ".............start........."
        echo "java -jar target/Sip4J-1.0-SNAPSHOT-jar-with-dependencies.jar" ${file}
#        echo `pwd`"/"${file}
        java -jar target/Sip4J-1.0-SNAPSHOT-jar-with-dependencies.jar ${file} $2
        echo ".............end..........."
    done
}

func $1 $2