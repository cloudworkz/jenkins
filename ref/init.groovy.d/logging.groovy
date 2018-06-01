import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter
import java.util.logging.LogManager
import jenkins.model.Jenkins

def Logger = LogManager.getLogManager().getLogger("hudson.WebAppMain")
Logger.addHandler(new ConsoleHandler())
