#!/bin/bash

./gradlew clean assemble
FILE=AdHoc-Railway/build/apk/AdHoc-Railway-debug-unaligned.apk
ssh baehnle@adhocserver 'rm -rf /var/www/adhoc-railway/artifacts-android && mkdir /var/www/adhoc-railway/artifacts-android'

scp $FILE baehnle@adhocserver:~/AdHoc-Railway/
scp $FILE baehnle@adhocserver:/var/www/adhoc-railway/artifacts-android
