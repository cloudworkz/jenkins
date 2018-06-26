import hudson.model.*
try {
    println("===> Configuring cleanup ...")

    // Cleaning up queue
    def queue = Hudson.instance.queue
    queue.clear()
    queue.items.each { queue.cancel(it.task) }

     // Cleaning up offline slaves...
    Hudson.instance.slaves.each {
        // if(it.getComputer().isOffline()) {
            it.getComputer().doDoDelete()
        // }
    }
    println("===> Configuring cleanup completed")
}
catch(Exception e) {
    println "===> Failed to configure cleanup: " + e
    System.exit(1)
}
