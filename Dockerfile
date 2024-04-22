FROM eclipse-temurin:21

### JAVA
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"

RUN rm /bin/sh && ln -s /bin/bash /bin/sh

RUN apt-get update \
  && apt-get install -y \
  curl \
  && rm -rf /var/lib/apt/lists/*

#### Scala
ENV SBT_VERSION=1.9.9
ENV SCALA_VERSION=3.3.1

# Install sbt
RUN curl -fL https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz | tar xz -C /usr/local && \
  ln -s /usr/local/sbt/bin/sbt /usr/bin/sbt

# Install Scala
RUN curl -L "https://github.com/scala/scala3/releases/download/${SCALA_VERSION}/scala3-${SCALA_VERSION}.tar.gz" | tar xz -C /usr/local && \
  ln -s /usr/local/scala-${SCALA_VERSION}/bin/scala /usr/bin/scala

WORKDIR /weather_app

### Weather
COPY . /weather_app

RUN sbt clean assembly

# Run the application
CMD ["java", "-jar", "/weather_app/target/scala-3.3.1/weather.jar"]