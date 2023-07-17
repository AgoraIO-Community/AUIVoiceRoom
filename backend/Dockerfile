FROM maven:3.9.2-eclipse-temurin-11 AS builder

WORKDIR /app

COPY mvn_settings.xml /root/.m2/settings.xml

COPY src ./src

COPY pom.xml ./

RUN mvn package -f pom.xml -Dmaven.test.skip=true -T 6


FROM ubuntu:20.04 AS Final

ENV JAVA_HOME=/usr/lib/jvm/default-java

# Change apt repository to a China network friendly site.
RUN sed -i s@/archive.ubuntu.com/@/mirrors.tuna.tsinghua.edu.cn/@g /etc/apt/sources.list
RUN sed -i s@/security.ubuntu.com/@/mirrors.tuna.tsinghua.edu.cn/@g /etc/apt/sources.list

RUN apt-get clean
RUN apt-get update

RUN apt-get install -y --no-install-recommends \
    apt-utils \
    software-properties-common \
    lsb-release \
    wget \
    curl \
    libssl-dev \
    libssl-dev \
    libcrypto++-dev \
    libc++-dev

# Add dependencies for RTC.
RUN apt-get install -y libxdamage1 \
    libxcomposite-dev \
    libdrm-dev

# Install java
RUN apt-get -y install default-jdk

ARG JAR_FILE
ARG VERSION
ARG BRANCH
ARG COMMIT_ID
ARG BUILD_DATE

ENV VERSION=${VERSION}
ENV BRANCH=${BRANCH}
ENV COMMIT_ID=${COMMIT_ID}
ENV BUILD_DATE=${BUILD_DATE}

WORKDIR /workspace

COPY --from=builder /app/target/*.jar /workspace/app.jar
COPY --from=builder /app/src/main/resources/lib/*.so /usr/lib/

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
