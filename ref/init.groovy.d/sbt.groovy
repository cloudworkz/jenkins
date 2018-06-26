import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*

try {
    plugin=Jenkins.instance.getExtensionList(org.jvnet.hudson.plugins.SbtPluginBuilder.DescriptorImpl.class)[0];
    tool = plugin.installations.find {
        it.name == "sbt"
    }
    if (tool == null) {
        println("===> Configuring sbt tool...")
        i=(plugin.installations as List);
        i.add(new org.jvnet.hudson.plugins.SbtPluginBuilder.SbtInstallation("sbt", "", "", []));
        plugin.installations=i
        plugin.save()
        println("===> Configuring sbt completed")
    }
}
catch(Exception e) {
    println "===> Failed to configure maven: " + e
    System.exit(1)
}
