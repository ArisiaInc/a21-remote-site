import json
from nacl.signing import VerifyKey
from nacl.exceptions import BadSignatureError
import os

# currently this is VERY VERBOSE we'll need to tone this down for real life
def lambda_handler(event, context): 
    print(json.dumps(event))
    body = json.loads(event['body'])
    PUBLIC_KEY = os.getenv('DISCORD_PUBKEY')
    verify_key = VerifyKey(bytes.fromhex(PUBLIC_KEY))
    try:
        signature = event["headers"]["x-signature-ed25519"]
        timestamp = event["headers"]["x-signature-timestamp"]
        str_body = event['body']
        verify_key.verify(f'{timestamp}{str_body}'.encode(), bytes.fromhex(signature))
    except BadSignatureError:
        print('bad signature error')
        return {
            'statusCode': 401,
            'body': json.dumps('invalid request signature')
        }
    except KeyError:
        print('cannot find keys')
        return {
            'statusCode': 401,
            'body': json.dumps('missing request signature')
        }

    print("body type is: {type}".format(**body))
    if body['type'] == 1:
        print('found a ping. responding with "type": 1')
        return {
            'statusCode': 200,
            'body': json.dumps({"type": 1})
        }
    # dummy response to everything
    if body['type'] == 2:
        print(json.dumps(body))
        print('hey look a message. let\'s respond')
        return {
            'statusCode': 200,
            'body': json.dumps({
                "type": 4,
                "data": {
                    "content": f"<@{body['member']['user']['id']}> your request has been entered. Linking your accounts now..."
                }
            })
        }