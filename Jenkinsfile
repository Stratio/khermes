@Library('libpipelines@master') _

hose {
    EMAIL = 'qa'
    MODULE = 'khermes'
    SLACKTEAM = 'stratiopaas'
    REPOSITORY = 'khermes'
    DEVTIMEOUT = 50
    RELEASETIMEOUT = 40    
    ATTIMEOUT = 240
    PKGMODULESNAMES = ['khermes']
    FOSS = true
	
 ITSERVICES = [
     ['ZOOKEEPER': [
                'image': 'jplock/zookeeper:3.5.2-alpha'],
                'sleep': 50,
                'healthcheck': 2181
                ],
     ['KAFKA':  [
                'image': 'confluent/kafka:0.10.0.0-cp1',
                'sleep': 50,
                'healthcheck': 9092,
                'env': ['KAFKA_ZOOKEEPER_CONNECT=%%ZOOKEEPER:2181']]]
]

ITPARAMETERS = """
        |    -Dkafka.bootstrap.servers=%%KAFKA:9092
        | """


    DEV = { config ->

        doCompile(config)
        parallel(UT: {
            doUT(config)
        }, IT: {
      //      doIT(config)
        }, failFast: config.FAILFAST)

	doPackage(config)

	parallel(QC: {
            doStaticAnalysis(config)
        }, DEPLOY: {
            doDeploy(config)
        }, failFast: config.FAILFAST)
        doDocker(config)
      }
}
