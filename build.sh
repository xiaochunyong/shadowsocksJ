#!/bin/bash
echo "execute -> mvn clean package -DskipTests"
mvn clean package -DskipTests

echo "execute -> ant bundle-shadowsocks"
ant bundle-shadowsocks