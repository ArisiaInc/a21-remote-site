#! /bin/sh

ts=`date +%s`

# uncomment to build or rebuild python packages
# you need docker for this
#rm -rf ./package/
#mkdir ./package
#docker run --rm -v "$PWD":/var/task lambci/lambda:build-python3.8 pip3 install --target ./package/ discord.py pynacl requests
cd package
zip -r ../deployment-$ts.zip .
cd ..
zip -g deployment-$ts.zip lambda_function.py
aws lambda update-function-code --function-name arisiabot --zip-file fileb://deployment-$ts.zip --no-cli-pager
rm deployment-$ts.zip
