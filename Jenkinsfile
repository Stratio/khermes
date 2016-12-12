@Library('libpipelines@master') _

hose {
    EMAIL = 'qa'
    MODULE = 'hermes'
    SLACKTEAM = 'stratiopaas'
    REPOSITORY = 'hermes' 
    DEVTIMEOUT = 50
    RELEASETIMEOUT = 40    
    ATTIMEOUT = 240


 ITSERVICES = [
     ['ZOOKEEPER': [
                'image': 'stratio/zookeeper:3.4.6'],
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
        |    -DKAFKA_HOST=%%KAFKA
        |    -DKAFKA_PORT=9092
        |    -DTOPIC_NAME=stratio
        | """


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
        doDocker(config)
      }
}
