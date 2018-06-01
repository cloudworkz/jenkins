import hudson.tools.*;
import hudson.tasks.Maven.MavenInstallation

try {
    println '===> Configuring Maven...'
    def name = "maven";
    def home = System.getenv("MAVEN_HOME") ?: "/opt/maven";
    def dis = ToolInstallation.all().get(MavenInstallation.DescriptorImpl.class)
    dis.setInstallations( new MavenInstallation(name, home, null));
    println("===> Configuring Maven completed")
}
catch(Exception e) {
    println "===> Failed to configure maven: " + e
    System.exit(1)
}
