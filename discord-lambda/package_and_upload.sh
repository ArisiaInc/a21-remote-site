#! /bin/sh

ts=`date +%s`

# uncomment to build or rebuild python packages
# you need docker for this
#rm -rf ./package/
#docker run --rm lambci/lambda:build-python3.8 -v "$PWD":/var/task pip3 install --target ./package/ discord.py pynacl
cd package
zip -r ../deployment-$ts.zip .
cd ..
zip -g deployment-$ts.zip lambda_function.py
aws lambda update-function-code --function-name arisiabot --zip-file fileb://deployment-$ts.zip --no-cli-pager
rm deployment-$ts.zip