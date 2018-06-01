FROM jenkins/jenkins:lts

ENV JENKINS_SHARE=/usr/share/jenkins \
    JENKINS_ADMIN_USERNAME=${JENKINS_ADMIN_USERNAME:-admin} \
    JENKINS_ADMIN_PASSWORD=${JENKINS_ADMIN_PASSWORD:-admin} \
    JENKINS_HOME=${JENKINS_HOME:-/var/jenkins_home} \
    JENKINS_NAME=${JENKINS_NAME:-google-cloud-tools}

COPY --chown=jenkins:jenkins ./ref/ ${JENKINS_SHARE}/ref/

RUN set -ex; \
    \
    # Install plugins
    /usr/local/bin/install-plugins.sh < ${JENKINS_SHARE}/ref/plugins.txt; \
    \
    # Indicate that this Jenkins installation is fully configured
    echo 2.0 > ${JENKINS_SHARE}/ref/jenkins.install.UpgradeWizard.state; \
    \
    # Configure master/slave secrets
    mkdir -p ${JENKINS_SHARE}/ref/secrets/; \
    echo "false" > ${JENKINS_SHARE}/ref/secrets/slave-to-master-security-kill-switch;
