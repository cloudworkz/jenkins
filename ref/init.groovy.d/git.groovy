import jenkins.model.*

try {
    println("===> Configuring Git ...")
    String gitName = System.getenv("JENKINS_GIT_NAME")
    String gitEmail = System.getenv("JENKINS_GIT_EMAIL")
    if (!gitName) {
        throw new RuntimeException("The JENKINS_GIT_NAME env should exist")
    }
    if (!gitEmail) {
        throw new RuntimeException("The JENKINS_GIT_EMAIL env should exist")
    }
    def inst = Jenkins.getInstance()
    def desc = inst.getDescriptor("hudson.plugins.git.GitSCM")
    desc.setGlobalConfigName(gitName)
    desc.setGlobalConfigEmail(gitEmail)
    desc.save()
    println("===> Configuring Git completed")
}
catch(Exception e) {
    println "===> Failed to configure git: " + e
    System.exit(1)
}
