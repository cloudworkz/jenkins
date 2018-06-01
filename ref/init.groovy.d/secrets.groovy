import jenkins.model.*
import hudson.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*;
import com.google.jenkins.plugins.credentials.oauth.*


try {
    println("===> Configuring secrets ...")
    Domain global_domain = Domain.global()
    SystemCredentialsProvider$StoreImpl credentials_store = Jenkins.instance.getExtensionList(
        'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
    )[0].getStore()

    // Git secrets
    String gitCredentialsId = System.getenv("JENKINS_GIT_CREDENTIALS_ID") ?: "jenkins-git-ssh-key"
    String gitUserName = System.getenv("JENKINS_GIT_NAME") ?: "jenkins"
    String gitPrivateSSHKeyPath = System.getenv("JENKINS_GIT_PRIVATE_SSH_KEY_PATH") ?: "~/.ssh/id_rsa"

    Credentials gitOldCredentials = credentials_store.getCredentials(global_domain).findResult {
        it.id == gitCredentialsId ? it : null
    }
    if (gitOldCredentials != null) {
        credentials_store.removeCredentials(global_domain, gitOldCredentials)
    }

    Credentials gitNewCredentials = new BasicSSHUserPrivateKey(
        CredentialsScope.GLOBAL,
        gitCredentialsId,
        gitUserName,
        new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource(
        gitPrivateSSHKeyPath), "", ""
    )
    credentials_store.addCredentials(global_domain, gitNewCredentials)

    // Google Cloud service account
    def saCredentialsId = System.getenv("SERVICE_ACCOUNT_CREDENTIALS_ID") ?: "google-cloud-service-account"
    def saKeyPath = System.getenv("SERVICE_ACCOUNT_CREDENTIALS_PATH")
    if (!saKeyPath) {
        throw new RuntimeException("The SERVICE_ACCOUNT_CREDENTIALS_PATH env should exist")
    }
    def googleProject = System.getenv("GOOGLE_PROJECT_ID")
    if (!googleProject) {
        throw new RuntimeException("The GOOGLE_PROJECT_ID env should exist")
    }
    def saKeyFile = new File(saKeyPath)
    if (!saKeyFile.exists()) {
        throw new RuntimeException("file '" + saKeyFile.getAbsolutePath() + "' should exist")
    }
    if (!saKeyFile.canRead()) {
        throw new RuntimeException("file '" + saKeyFile.getAbsolutePath() + "' should be readable")
    }
    if (saKeyFile.length() < 1) {
        throw new RuntimeException("file '" + saKeyFile.getAbsolutePath() + "' should have content")
    }
    def keyFile = new FileParameterValue.FileItemImpl(saKeyFile)
    def serviceAccountConfig = new JsonServiceAccountConfig(keyFile, null)
    def saCredentials = new GoogleRobotPrivateKeyCredentials(googleProject, serviceAccountConfig, null)
    Credentials saOldCredentials = credentials_store.getCredentials(global_domain).findResult {
        it.id == saCredentialsId ? it : null
    }
    if (saOldCredentials != null) {
        credentials_store.removeCredentials(global_domain, saOldCredentials)
    }
    credentials_store.addCredentials(global_domain, saCredentials)

    println("===> Configuring secrets completed")
}
catch(Exception e) {
    println "===> Failed to configure seed: " + e
    System.exit(1)
}
