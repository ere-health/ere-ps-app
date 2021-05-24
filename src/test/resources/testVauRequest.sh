#!/bin/bash
(cat BEFORE-VAU-RAW-Request.txt; sleep 1) | openssl s_client -connect fd.erezept-instanz1.titus.ti-dienste.de:443
