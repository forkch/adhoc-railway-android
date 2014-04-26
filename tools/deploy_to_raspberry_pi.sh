#!/bin/bash

./gradlew clean assemble
FILE=AdHoc-Railway/build/apk/AdHoc-Railway-debug-unaligned.apk
scp $FILE baehnle@adhocserver:~/AdHoc-Railway/
scp $FILE baehnle@adhocserver:/var/www/adhoc-railway/
