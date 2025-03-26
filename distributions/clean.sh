#!/bin/bash

echo 'Cleaning package...'

DPKG_BUILD_PACKAGE="ere-health"
DEB_VERSION="1.0.0"
PATCH="15"

rm -R ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}.orig.tar.gz
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-${PATCH}_amd64.build
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-${PATCH}_amd64.buildinfo
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-${PATCH}_amd64.changes
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-${PATCH}.debian.tar.xz
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-${PATCH}.dsc
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-${PATCH}_all.deb
rm -R deb-package/debian/ere-health
rm -R deb-package/debian/.debhelper
