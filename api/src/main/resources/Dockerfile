FROM openjdk:11-jre

MAINTAINER guojiaxing<302802003@qq.com>

EXPOSE 5000

COPY ./target/*.jar /opt/easyJmeter.jar

COPY ./src/main/resources/docker-entrypoint.sh /opt/docker-entrypoint.sh

WORKDIR /opt/

ENV JMETER_HOME=/opt/apache-jmeter

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone

RUN apt-get update && apt-get install -y procps

ENTRYPOINT ["sh", "./docker-entrypoint.sh"]

CMD ["server", "prod"]





















