#!/bin/bash
# Paste Data from ReadCardCertificates Call (C.QES)
vi ehba-base64.dat
# Create der certificate
base64 -d ehba-base64.dat > ehba.der
# Create pem certificate
openssl x509 -inform DER -in ehba.der > ehba.pem
# Create PEM Description
openssl x509 -in ehba.pem -text > ehba.txt
