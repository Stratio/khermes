#!/bin/bash

#Build khermes image

if [ ! -e ../../target/khermes-0.3.0-SNAPSHOT-allinone.jar ]
then
    cd ../..
    mvn clean package
    cd scripts/quickstart
else
   if [ "$1" = "package" ]
    then
      cd ../..
      mvn clean package
      cd scripts/quickstart
   fi
fi

echo "copying khermes jar..."
cp ../../target/khermes-0.3.0-SNAPSHOT-allinone.jar .

echo "copying khermes startup script ..."
cp ../seed ./seed.sh

echo -e "#!/bin/sh\n$(cat seed.sh)" > input
cat input > seed.sh

sed -i -e 's"../target"/khermes"g' seed.sh

echo "building khermes image..."
docker build . -t khermes

echo "checking path..."
if [ -z ${LOCALPATH+x} ]; then
  echo "LOCALPATH var is not set, using current directory"
  export LOCALPATH=.
else
  export LOCALPATH=$LOCALPATH
  echo "using ${LOCALPATH} as workdir"
fi

rm seed
rm khermes-0.3.0-SNAPSHOT-allinone.jar
rm input
rm seed.sh-e

#start services
echo "Stating docker..."
docker-compose -f compose.yml up zookeeper khermes
