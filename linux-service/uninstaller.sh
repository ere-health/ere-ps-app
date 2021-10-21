#!/bin/bash
sudo systemctl stop ere-health
sudo systemctl disable ere-health.service
sudo rm -R /opt/ere-health
sudo rm /etc/systemd/system/ere-health.service
echo "ere-health successfully uninstalled"
