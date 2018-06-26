import jenkins.model.*
import org.jenkins.ci.plugins.html5_notifier.*

try {
    println("===> Configuring notifier...")
    def instance = Jenkins.getInstance()
    def descriptor = instance.getDescriptor(GlobalConfigurationImpl.class)
    descriptor.setEnabled(true)
    descriptor.setQueryTimeout(5)
    descriptor.setNotificationTimeout(15000)
    descriptor.setAllResults(true)
    instance.save()
    println("===> Configuring notifier completed")
}
catch(Exception e) {
    println "===> Failed to configure notifier: " + e
    System.exit(1)
}
