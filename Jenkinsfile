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
        parallel(UT: {
            doUT(config)
        }, IT: {
            doIT(config)
        }, failFast: config.FAILFAST)

	doPackage(config)

	parallel(QC: {
            doStaticAnalysis(config)
        }, DEPLOY: {
            doDeploy(config)
        }, failFast: config.FAILFAST)
     }
}
