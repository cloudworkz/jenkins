def jobs = [
    [name: 'Jenkins Setup', repo: 'git@github.com:google-cloud-tools/jenkins-setup.git', branch: '*/master', ignoreScm: '.*Seed\\.groovy'],
]

def multibranchJobs = [
    [name: 'Jenkins Setup All Branches', repo: 'git@github.com:google-cloud-tools/jenkins-setup.git', branch: '*/master', ignoreScm: '.*Seed\\.groovy'],
]

//trigger
jobs.each { i ->
    job = pipelineJob(i.name) {
        if (i.disabled != null && i.disabled) {
            disabled()
        }
        logRotator() {
            artifactNumToKeep(30)
            numToKeep(30)
        }
        definition {
            cpsScm {
                scm {
                    git {
                        remote {
                            url("${i.repo}")
                            credentials('jenkins-git-ssh-key')
                            if(i.refspec != null) {
                                refspec(i.refspec)
                            }
                        }
                        branch(i.branch)
                        if (i.ignoreScm != null) {
                            extensions {
                                pathRestriction {
                                    includedRegions('')
                                    excludedRegions(i.ignoreScm)
                                }
                            }
                        }
                    }
                    if (i.jenkinsfile != null) {
                        scriptPath(i.jenkinsfile)
                    } else {
                        scriptPath('Jenkinsfile')
                    }
                }
            }
        }
        triggers {
            if(i.scmtrigger != null) {
                scm(i.scmtrigger)
            } else {
                scm('H/5 * * * *')
            }
            if (i.upstreams != null) {
                upstream(i.upstreams.join(', '), 'SUCCESS')
            }
        }
        if (i.upstreams != null) {
            blockOnUpstreamProjects()
        }
    }

}

multibranchJobs.each { job ->
    multibranchPipelineJob(job.name) {
        branchSources {
            git {
                remote("${i.repo}")
                excludes('master')
            }
        }
        orphanedItemStrategy {
            discardOldItems {
                numToKeep(0)
            }
        }
        triggers {
            periodic(1)
        }
    }
}