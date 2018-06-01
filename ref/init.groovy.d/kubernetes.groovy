import hudson.model.*
import jenkins.model.*
import org.csanchez.jenkins.plugins.kubernetes.*
import org.csanchez.jenkins.plugins.kubernetes.volumes.workspace.EmptyDirWorkspaceVolume
import org.csanchez.jenkins.plugins.kubernetes.volumes.HostPathVolume

import org.csanchez.jenkins.plugins.kubernetes.model.KeyValueEnvVar

def kc
try {
    println("===> Configuring k8s...")
    String jenkinsName = System.getenv("JENKINS_NAME")
    String namespace = System.getenv("NAMESPACE")
    String agentImage = System.getenv("JENKINS_AGENT_IMAGE")
    String agentNodeSelector = System.getenv("JENKINS_AGENT_NODE_SELECTOR")
    def agentRequestCpu = System.getenv("JENKINS_AGENT_REQUEST_CPU") ?: "0.2"
    def agentLimitCpu = System.getenv("JENKINS_AGENT_LIMIT_CPU") ?: "1"
    def agentRequestMemory = System.getenv("JENKINS_AGENT_REQUEST_MEMORY") ?: "512Mi"
    def agentLimitMemory = System.getenv("JENKINS_AGENT_LIMIT_MEMORY") ?: "512Mi"

    if (Jenkins.instance.clouds) {
        kc = Jenkins.instance.clouds.get(0)
        println "Cloud config found: ${Jenkins.instance.clouds}"
    } else {
        kc = new KubernetesCloud("kubernetes")
        Jenkins.instance.clouds.add(kc)
        println "Cloud config added: ${Jenkins.instance.clouds}"
    }

    kc.setServerUrl("https://kubernetes.default")
    kc.setJenkinsUrl("http://${jenkinsName}-jenkins.${namespace}.svc.cluster.local:8080")
    kc.setJenkinsTunnel("${jenkinsName}-jenkins-agent.${namespace}.svc.cluster.local:50000")
    kc.setSkipTlsVerify(false)
    kc.setNamespace("${namespace}")
    // kc.setCredentialsId("")
    kc.setRetentionTimeout(5)
    kc.setConnectTimeout(5)
    kc.setReadTimeout(15)
    kc.setMaxRequestsPerHostStr("32")
    kc.setContainerCapStr("10")

    if(kc.templates) {
        kc.templates.clear()
    } else {
        kc.templates = []
    }
    def podTemplate = new PodTemplate()
    podTemplate.setLabel("jenkins-agent")
    podTemplate.setName("${jenkinsName}-agent")
    // podTemplate.setInheritFrom("")
    // podTemplate.setSlaveConnectTimeout(0)
    if(agentNodeSelector) {
        podTemplate.setNodeSelector(agentNodeSelector)
    }
    podTemplate.setCommand(null)
    podTemplate.setInstanceCapStr('10')
    podTemplate.setIdleMinutesStr('30')
    podTemplate.setNodeUsageMode('NORMAL')
    // podTemplate.setWorkspaceVolume(new EmptyDirWorkspaceVolume(false))

    def volumes = []
    volumes << new HostPathVolume("/usr/bin/docker", "/usr/bin/docker")
    volumes << new HostPathVolume("/var/run/docker.sock", "/var/run/docker.sock")
    podTemplate.setVolumes(volumes)

    def envVars = []
    envVars << new KeyValueEnvVar("NAMESPACE", namespace)
    podTemplate.setEnvVars(envVars)

    ContainerTemplate ct = new ContainerTemplate("jnlp", agentImage)
    ct.setAlwaysPullImage(true)
    ct.setPrivileged(false)
    ct.setTtyEnabled(true)
    ct.setWorkingDir("/home/jenkins")
    ct.setArgs('${computer.jnlpmac} ${computer.name}')
    ct.setResourceRequestCpu(agentRequestCpu)
    ct.setResourceLimitCpu(agentLimitCpu)
    ct.setResourceRequestMemory(agentRequestMemory)
    ct.setResourceLimitMemory(agentLimitMemory)
    podTemplate.setContainers([ct])
    kc.templates << podTemplate
    kc = null
    println("===> Configuring k8s completed")
}
catch(Exception e) {
    println "===> Failed to configure kubernetes: " + e
    System.exit(1)
}
finally {
    kc = null
}
