@Library('libpipelines@feature/multibranch') _

hose {
    EMAIL = 'qa'
    MODULE = 'hermes'
    SLACKTEAM = 'stratiopaas'
    REPOSITORY = 'hermes' 
    DEVTIMEOUT = 50
    RELEASETIMEOUT = 40    
    ATTIMEOUT = 240

    DEV = { config ->

        doCompile(config)
        doUT(config)
        doIT(config)
        doPackage(config)
        doDeploy(config)
     }
}
