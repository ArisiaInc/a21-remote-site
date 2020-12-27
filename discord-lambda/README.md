#The lambda discord bot

This is all written in python 3. 3.8+ specifically.

## Testing the lambda locally

There's a neat docker container that lets you do this pretty easily.
you'll need to set DISCORD_PUBKEY as an environment variable first (this is available in application information)

`docker run --rm -v "$PWD":/var/task:ro,delegated -e DISCORD_PUBKEY lambci/lambda:python3.8 lambda_function.lambda_handler '{"payload": "goes here"}'`

## Uploading the lambda
Note: the first time you run ./package_and_upload.sh, you'll need to uncomment the first 2 lines. You'll also need docker installed.

You'll also need to have working aws command line access.

`./package_and_upload.sh`

## Registering the endpoints with a specific guild

1. take `template.env` and rename it `.env` and then fill it out with the relevant values
2. `python register_endpoints.py`