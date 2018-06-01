import jenkins.model.*
import hudson.tools.*;
import java.util.List;
import com.cloudbees.jenkins.plugins.gcloudsdk.GCloudInstallation

try {
    println("===> Configuring GCloud ...")
    def name = "gcloud-default"
    def home = "/opt/google-cloud-sdk"
    def desc = ToolInstallation.all().get(GCloudInstallation.DescriptorImpl.class)
    desc.setInstallations( new GCloudInstallation(name, home, null));
    desc.save()
    println("===> Configuring GCloud completed")
}
catch(Exception e) {
    println "===> Failed to configure gcloud: " + e
    System.exit(1)
}
