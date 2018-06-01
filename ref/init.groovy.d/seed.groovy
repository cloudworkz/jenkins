import jenkins.model.*
import hudson.model.*
import hudson.plugins.git.*
import javaposse.jobdsl.plugin.*
import hudson.plugins.git.BranchSpec
import hudson.plugins.git.GitSCM
import hudson.plugins.git.UserRemoteConfig
import hudson.triggers.SCMTrigger
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

try {
    if ("${System.getenv("JENKINS_SEED_ENABLED")}" != "false") {
        def jobName = "Seed"
        println("===> Configuring ${jobName} ...")
        String gitCredentialsId = System.getenv("JENKINS_GIT_CREDENTIALS_ID") ?: "jenkins-git-ssh-key"
        String seedRepo = System.getenv("JENKINS_SEED_REPO")
        String seedTarget = System.getenv("JENKINS_SEED_TARGET")
        if (!seedRepo) {
            throw new RuntimeException("The JENKINS_SEED_REPO env should exist")
        }
        if (!seedTarget) {
            throw new RuntimeException("The JENKINS_SEED_TARGET env should exist")
        }
        def job
        Jenkins.instance.items.findAll { j -> j.name == jobName }
            .each { j -> job = j }

        if(!job) {
            job = Jenkins.instance.createProject(FreeStyleProject.class, jobName)
        }
        job.setAssignedLabel(null);
        job.addTrigger(new SCMTrigger('H/2 * * * *'))
        def remote = new UserRemoteConfig(seedRepo, null, null, gitCredentialsId)
        def scm = new GitSCM(
            [remote] as List, [new BranchSpec("master")],
            false, [], null, null, [])
        job.scm = scm
        def executeDslScripts = new ExecuteDslScripts()
        executeDslScripts.setTargets(seedTarget)
        executeDslScripts.setRemovedJobAction(RemovedJobAction.DELETE)
        executeDslScripts.setRemovedViewAction(RemovedViewAction.IGNORE)
        executeDslScripts.setLookupStrategy(LookupStrategy.JENKINS_ROOT)
        job.buildersList.clear()
        job.buildersList.add(executeDslScripts)
        Jenkins.instance.save()
        println("===> Configuring Seed completed")
    }
}
catch(Exception e) {
    println "===> Failed to configure seed: " + e
    System.exit(1)
}
