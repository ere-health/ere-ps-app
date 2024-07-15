#!/bin/bash

echo 'Cleaning package...'

DPKG_BUILD_PACKAGE="ere-health"
DEB_VERSION="1.0.0"

rm -R ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}.orig.tar.gz
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-1_amd64.build
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-1_amd64.buildinfo
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-1_amd64.changes
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-1.debian.tar.xz
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-1.dsc
rm ${DPKG_BUILD_PACKAGE}_${DEB_VERSION}-1_all.deb
rm -R deb-package/debian/ere-health
rm -R deb-package/debian/.debhelper
