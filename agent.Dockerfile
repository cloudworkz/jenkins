FROM jenkins/jnlp-slave:3.19-1

ENV \
    ### JDK
    JAVA_VERSION_MAJOR=8 \
    JAVA_VERSION_MINOR=131 \
    JAVA_VERSION_BUILD=11 \
    JAVA_PACKAGE=jdk \
    JAVA_DOWNLOAD_HASH=d54c1d3a095b4ff2b6607d096fa80163 \
    JAVA_HOME=/opt/jdk \
    ### Maven
    MAVEN_HOME=/opt/maven \
    MAVEN_VERSION=3.3.9 \
    MAVEN_DEFAULT_OPTS=-Xmx512m \
    MVN_OPTS=${MVN_OPTS:-${MAVEN_DEFAULT_OPTS}} \
    APACHE_MIRROR_BASE_URL=http://ftp.halifax.rwth-aachen.de/apache \
    MAVEN_MD5_CHECKSUM=516923b3955b6035ba6b0a5b031fbd8b \
    MAVEN_DL_TMP_FILE=/tmp/apache-maven.tar.gz \
    ### GCloud SDK
    GCLOUD_HOME=/opt/google-cloud-sdk \
    CLOUDSDK_PYTHON_SITEPACKAGES=1 \
    GCLOUD_COMPONENTS="kubectl alpha beta gcd-emulator cloud-datastore-emulator bigtable" \
    DEBIAN_FRONTEND=noninteractive \
    ### Defaults
    PATH=${PATH}:${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${GCLOUD_HOME}/bin

USER root

RUN set -ex; \
    ### Install JDK
    curl -jksSLH "Cookie: oraclelicense=accept-securebackup-cookie" \
    http://download.oracle.com/otn-pub/java/jdk/${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-b${JAVA_VERSION_BUILD}/${JAVA_DOWNLOAD_HASH}/${JAVA_PACKAGE}-${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-linux-x64.tar.gz \
    | tar -xzf - -C /opt; \
    \
    ln -s /opt/${JAVA_PACKAGE}1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_MINOR} /opt/jdk; \
    \
    rm -rf /opt/jdk/*src.zip \
        /opt/jdk/lib/missioncontrol \
        /opt/jdk/lib/visualvm \
        /opt/jdk/lib/*javafx* \
        /opt/jdk/db \
        /opt/jdk/jre/lib/plugin.jar \
        /opt/jdk/jre/lib/ext/jfxrt.jar \
        /opt/jdk/jre/bin/javaws \
        /opt/jdk/jre/lib/javaws.jar \
        /opt/jdk/jre/lib/desktop \
        /opt/jdk/jre/plugin \
        /opt/jdk/jre/lib/deploy* \
        /opt/jdk/jre/lib/*javafx* \
        /opt/jdk/jre/lib/*jfx* \
        /opt/jdk/jre/lib/amd64/libdecora_sse.so \
        /opt/jdk/jre/lib/amd64/libprism_*.so \
        /opt/jdk/jre/lib/amd64/libfxplugins.so \
        /opt/jdk/jre/lib/amd64/libglass.so \
        /opt/jdk/jre/lib/amd64/libgstreamer-lite.so \
        /opt/jdk/jre/lib/amd64/libjavafx*.so \
        /opt/jdk/jre/lib/amd64/libjfx*.so; \
    \
    ### Install maven
    mkdir -p ${MAVEN_HOME}; \
    curl -fsSL -o ${MAVEN_DL_TMP_FILE} ${APACHE_MIRROR_BASE_URL}/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz; \
    echo "${MAVEN_MD5_CHECKSUM} ${MAVEN_DL_TMP_FILE}" | md5sum -c -; \
    tar -xzf ${MAVEN_DL_TMP_FILE} -C ${MAVEN_HOME} --strip-components=1; \
    rm -f ${MAVEN_DL_TMP_FILE}; \
    \
    ### Install google cloud SDK
    apt-get update; \
    apt-get install -y -qq --no-install-recommends wget zip unzip python python-dev python-pip build-essential openssh-client python-openssl jq; \
    apt-get clean; \
    cd /opt; \
    wget https://dl.google.com/dl/cloudsdk/channels/rapid/google-cloud-sdk.zip; \
    unzip google-cloud-sdk.zip; \
    rm google-cloud-sdk.zip; \
    google-cloud-sdk/install.sh --usage-reporting=false --rc-path=/.bashrc --additional-components ${GCLOUD_COMPONENTS}; \
    pip install --upgrade google-api-python-client google-auth-httplib2 google-cloud;

USER jenkins