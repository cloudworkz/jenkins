import jenkins.model.*
import jenkins.CLI
import hudson.model.Node.Mode
import hudson.markup.RawHtmlMarkupFormatter
import hudson.security.csrf.DefaultCrumbIssuer

def jenkinsName = System.getenv("JENKINS_NAME") ?: "k8s"
def welcomeMessage = """
<div style="word-wrap: break-word; max-width: 100%;">
    <h3>Welcome to ${jenkinsName.toUpperCase()} Jenkins.</h3>
    <style>
        .wrap-collabsible {
            margin-bottom: 1.2rem 0;
        }
        input[type='checkbox'] {
            display: none;
        }
        .lbl-toggle {
            display: block;
            font-weight: bold;
            font-family: monospace;
            text-transform: uppercase;
            cursor: pointer;
            border-radius: 7px;
            transition: all 0.25s ease-out;
        }
        .lbl-toggle:hover {
            color: #444;
        }
        .lbl-toggle::before {
            content: ' ';
            display: inline-block;
            border-top: 5px solid transparent;
            border-bottom: 5px solid transparent;
            border-left: 5px solid currentColor;
            vertical-align: middle;
            margin-right: .7rem;
            transform: translateY(-2px);
            transition: transform .2s ease-out;
        }
        .toggle:checked + .lbl-toggle::before {
            transform: rotate(90deg) translateX(-3px);
        }
        .collapsible-content {
            max-height: 0px;
            overflow: hidden;
            transition: max-height .25s ease-in-out;
        }
        .toggle:checked + .lbl-toggle + .collapsible-content {
            max-height: 350px;
        }
        .toggle:checked + .lbl-toggle {
            border-bottom-right-radius: 0;
            border-bottom-left-radius: 0;
        }
        .collapsible-content .content-inner {
            background: rgba(250, 224, 66, .2);
            border-bottom: 1px solid rgba(250, 224, 66, .45);
            border-bottom-left-radius: 7px;
            border-bottom-right-radius: 7px;
            padding: .5rem 1rem;
        }
    </style>
    <div class="wrap-collabsible">
    <input id="collapsible" class="toggle" type="checkbox">
    <label for="collapsible" class="lbl-toggle">Git public key...</label>
    <div class="collapsible-content">
        <div class="content-inner">
        <p>${System.getenv("JENKINS_GIT_PUBLIC_SSH_KEY")}</p>
        </div>
    </div>
    </div>
</div>
"""

// http://javadoc.jenkins-ci.org/index.html?jenkins/model/Jenkins.html
try {
    println("===> Configuring defaults ...")
    Jenkins.instance.setNumExecutors(0)
    Jenkins.instance.setMode(Mode.EXCLUSIVE)
    Jenkins.instance.setDisableRememberMe(false)
    Jenkins.instance.setSystemMessage(welcomeMessage)
    Jenkins.instance.setScmCheckoutRetryCount(0)
    Jenkins.instance.setQuietPeriod(0)

    Set<String> agentProtocolsList = ['JNLP4-connect', 'Ping']
    Jenkins.instance.setAgentProtocols(agentProtocolsList)
    Jenkins.instance.setSlaveAgentPort(5000)

    def enableCli = CLI.get()
    enableCli.setEnabled(false)

    if(Jenkins.instance.getCrumbIssuer() == null) {
        Jenkins.instance.setCrumbIssuer(new DefaultCrumbIssuer(true))
    }

    Jenkins.instance.setMarkupFormatter(new RawHtmlMarkupFormatter(false))
    Jenkins.instance.save()

    println("===> Configuring defaults completed")
}
catch(Exception e) {
    println "===> Failed to configure defaults: " + e
    System.exit(1)
}
