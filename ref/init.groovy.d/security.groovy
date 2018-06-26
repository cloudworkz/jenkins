import java.lang.System
import jenkins.model.*
import jenkins.security.*
import jenkins.security.s2m.AdminWhitelistRule
import hudson.model.*
import hudson.scm.*
import hudson.security.*
import hudson.util.Secret
import com.cloudbees.plugins.credentials.*
import org.jenkinsci.plugins.googlelogin.GoogleOAuth2SecurityRealm
import org.jenkinsci.plugins.googleadmin.GoogleAdminService
import javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration

try {
    println '===> Configure Admin Credentials...'
    def instance = Jenkins.getInstance()
    def userName = System.getenv("JENKINS_ADMIN_USERNAME")
    def userPassword = System.getenv("JENKINS_ADMIN_PASSWORD")
    def hudsonRealm = new HudsonPrivateSecurityRealm(false)
    hudsonRealm.createAccount(userName, userPassword)
    instance.setSecurityRealm(hudsonRealm)
    def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
    instance.setAuthorizationStrategy(strategy)
    instance.save()
    Jenkins.instance.getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false)
    // This token will be used to call prestop script
    User u = User.get("admin")
    ApiTokenProperty t = u.getProperty(ApiTokenProperty.class)
    def apiToken = t.getApiTokenInsecure()
    def authTokenFile = new File("${System.getenv("JENKINS_HOME")}/.api_token")
    authTokenFile.write "$apiToken"
    println("===> Configuring Admin Credentials completed")

    if ("${System.getenv("GOOGLE_AUTH_ENABLED")}" == "true") {
        println '===> Configuring Google Auth Login...'
        def domain = System.getenv("GOOGLE_AUTH_ALLOWED_DOMAIN")
        def clientId = System.getenv("GOOGLE_AUTH_CLIENT_ID")
        def clientSecret = System.getenv("GOOGLE_AUTH_CLIENT_SECRET")
        Secret secret = new Secret(clientSecret)
        String encryptedClientSecret = secret.getEncryptedValue()
        SecurityRealm googleLogin_realm = new GoogleOAuth2SecurityRealm(clientId, clientSecret, domain)
        Jenkins.instance.setSecurityRealm(googleLogin_realm)
        def authAtrategy = new hudson.security.GlobalMatrixAuthorizationStrategy()
        authAtrategy.add(Jenkins.ADMINISTER, 'admin')

        if ("${System.getenv("GOOGLE_ADMIN_ENABLED")}" == "true") {
            println '===> Configuring Google Groups...'
            String adminAccountEmail = System.getenv("GOOGLE_ADMIN_ACCOUNT_EMAIL")
            if (!adminAccountEmail) {
                throw new RuntimeException("The GOOGLE_ADMIN_ACCOUNT_EMAIL env should exist")
            }

            String clientSecretFile = System.getenv("GOOGLE_ADMIN_CLIENT_SECRET_FILE")
            if (!clientSecretFile) {
                throw new RuntimeException("The GOOGLE_ADMIN_ACCOUNT_EMAIL env should exist")
            }

            def googleService = new GoogleAdminService(clientSecretFile, adminAccountEmail)

            String administerGroupKey = System.getenv("GOOGLE_ADMIN_ADMINISTER_GROUP_EMAIL")
            if (administerGroupKey) {
                String[] administerUsersArray = googleService.getGroupMembers(administerGroupKey)
                administerUsersArray.each{
                    authAtrategy.add(Jenkins.ADMINISTER, "${it}")
                }
            }

            String readGroupKey = System.getenv("GOOGLE_ADMIN_READ_GROUP_EMAIL")
            if (readGroupKey) {
                String[] readUsersArray = googleService.getGroupMembers(readGroupKey)
                readUsersArray.each{
                    authAtrategy.add(Hudson.READ, "${it}")
                    authAtrategy.add(Item.WORKSPACE, "${it}")
                    authAtrategy.add(Item.BUILD, "${it}")
                    authAtrategy.add(Item.DISCOVER, "${it}")
                    authAtrategy.add(Item.READ, "${it}")
                    authAtrategy.add(SCM.TAG, "${it}")
                    authAtrategy.add(Computer.BUILD, "${it}")
                    authAtrategy.add(Computer.CONNECT, "${it}")
                    authAtrategy.add(Computer.DISCONNECT, "${it}")
                    authAtrategy.add(CredentialsProvider.VIEW, "${it}")
                    authAtrategy.add(Job.BUILD, "${it}")
                    authAtrategy.add(Job.READ, "${it}")
                    authAtrategy.add(Job.CANCEL, "${it}")
                    authAtrategy.add(View.READ, "${it}")
                }
            }
            println("===> Configuring Google Groups completed")
        }

        Jenkins.instance.setAuthorizationStrategy(authAtrategy)
        Jenkins.instance.save()
        println("===> Configuring Google Auth Login completed")

        def globalConfiguration = GlobalConfiguration.all().get(GlobalJobDslSecurityConfiguration.class)
        if ("${System.getenv("JENKINS_USE_SCRIPT_SECURITY")}" == "true") {
            globalConfiguration.useScriptSecurity = true
        } else {
            globalConfiguration.useScriptSecurity = false
        }
        globalConfiguration.save()
    }
}
catch(Exception e) {
    println "===> Failed to configure security: " + e
    System.exit(1)
}
