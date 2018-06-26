import jenkins.model.*
import java.net.URI
import com.cloudbees.syslog.sender.UdpSyslogMessageSender
import jenkins.plugins.logstash.*
import jenkins.plugins.logstash.persistence.*
import jenkins.plugins.logstash.configuration.*
import com.cloudbees.syslog.MessageFormat;
import jenkins.plugins.logstash.persistence.LogstashIndexerDao.SyslogProtocol;
import hudson.tools.ToolProperty

try {
    if ("${System.getenv("LOGGING_ENABLED")}" == "true") {
        println("===> Configuring logstash...")
        def config = GlobalConfiguration.all().get(LogstashConfiguration.class)
        ElasticSearch elastic = new ElasticSearch();
        URL url = new URL(System.getenv("LOGGING_URL"))
        elastic.setUri(url.toURI());

        config.setLogstashIndexer(elastic);
        config.setEnableGlobally(true);
        config.save();

        println("===> Configuring logstash completed")
    }
}
catch(Exception e) {
    println "===> Failed to configure logstash: " + e
    System.exit(1)
}
