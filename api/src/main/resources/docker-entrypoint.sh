
#!/bin/sh

set -e

if [ $1 = "server" ];then
        java -jar -Dsocket.server.enable=true -Dsocket.client.enable=false  easyJmeter.jar --spring.profiles.active=$2
elif [ $1 = "agent" ];then
        java -jar -Dsocket.server.enable=false -Dsocket.client.enable=true easyJmeter.jar --spring.profiles.active=$2
else
        echo "参数错误 1) server 作为后端主程序启动. 2）agent 作为压力机监听器启动"
        exit 1
fi
