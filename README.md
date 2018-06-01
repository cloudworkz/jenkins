# Google Cloud Jenkins

[![Docker Repository on Quay](https://quay.io/repository/google-cloud-tools/jenkins-agent/status "Docker Repository on Quay")](https://quay.io/repository/google-cloud-tools/jenkins-agent)
[![Docker Repository on Quay](https://quay.io/repository/google-cloud-tools/jenkins-agent/status "Docker Repository on Quay")](https://quay.io/repository/google-cloud-tools/jenkins-agent)

Full featured and ready to use Jenkins Setup in the Google Cloud Kubernetes Engine.

## Feature

- All configurations are scripted with Jenkins Post-Init Groovy scripts and could be enabled or disabled via environment variables
- Basic security is enabled by default
- Authentication are configured with Google Login
- Authorisation are configured Google Groups and Global Matrix Authorization Strategy
- Provision jenkins credentials from k8s secrets
- Nginx Ingress default configuration with security headers
- Git plugin default configuration
- Kubernetes plugin configuration
- GCloud tool default installation
- Maven tool default installation
- JDK default installation
- Material Design Theme by default
- Seed jobs configuration
