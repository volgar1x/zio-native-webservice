FROM ghcr.io/graalvm/graalvm-community:20

RUN curl -fLo /usr/bin/coursier https://github.com/coursier/launchers/raw/master/coursier && chmod +x /usr/bin/coursier && \
  yes no | /usr/bin/coursier setup --dir /usr/bin

RUN mkdir -p /webservice
WORKDIR /webservice
COPY project ./project
COPY src ./src
COPY build.sbt .

RUN sbt graalvm-native-image:packageBin

FROM scratch
COPY --from=0 /webservice/target/graalvm-native-image/webservice /bin/webservice
CMD ["/bin/webservice"]
