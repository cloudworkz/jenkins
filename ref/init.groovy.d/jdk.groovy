import jenkins.model.*

try {
    println("===> Configuring JDK ...")
    def name = 'jdk'
    def home = System.getenv("JDK_HOME") ?: '/opt/jdk'
    def jdk = new hudson.model.JDK(name, home);
    def jdklist = []
    jdklist.add(jdk)
    jenkins.model.Jenkins.instance.JDKs = jdklist;
    println("===> Configuring JDK completed")
}
catch(Exception e) {
    println "===> Failed to configure jdk: " + e
    System.exit(1)
}
