#!/bin/bash

echo 'Building package...'

DPKG_BUILD_PACKAGE="ere-health"
DEB_VERSION="1.0.0"

mkdir ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}
mkdir -p ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}/src
cp -r ../src/main ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}/src
cp -r ../pom.xml ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}/pom.xml
cp -r ../KBV_FHIR_eRP_V1_1_0 ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}/
mkdir ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}/config
cp ../src/main/resources/application.properties ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}/config/
mkdir ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}/log
cp -r ../prometheus ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}/
cp ../linux-service/run.sh ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}/
# Polykit rules file
mvn -U dependency:copy -Dartifact=io.prometheus.jmx:jmx_prometheus_javaagent:1.0.1:jar -DoutputDirectory=${DPKG_BUILD_PACKAGE}_${DEB_VERSION}/
cp ../linux-service/${DPKG_BUILD_PACKAGE}.service deb-package/debian/

tar czf ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}.orig.tar.gz ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}/
cd deb-package
debuild -us -uc
cd ..
dpkg-sig --sign builder ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-7_all.deb
# reprepro -V --basedir ~/public_html/apt/ includedeb jammy ere-health_1.0.0-7_all.deb
# rsync -va /home/manuel/public_html/apt/ ec2-user@packages.service-health.de:/srv/www/vhosts/packages.service-health.de
