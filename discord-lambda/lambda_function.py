import json
from nacl.signing import VerifyKey
from nacl.exceptions import BadSignatureError
import os
import requests

BASE_API_URL = "https://discord.com/api/v8"
DISCORD_GUILD_ID = os.getenv('DISCORD_GUILD_ID')
DISCORD_BOT_TOKEN = os.getenv('DISCORD_BOT_TOKEN')
GUILD_FRAGMENT = f"guilds/{DISCORD_GUILD_ID}"

SAFER_SPACE_CHANNELS = {
    'lgbtqiap': os.getenv('DISCORD_LGBTQ_SS')
}

def get_api_headers():
    return {'X-Shared-Secret': os.getenv('API_SECRET')}

def bad_signature(event):
    PUBLIC_KEY = os.getenv('DISCORD_PUBKEY')
    verify_key = VerifyKey(bytes.fromhex(PUBLIC_KEY))
    try:
        signature = event["headers"]["x-signature-ed25519"]
        timestamp = event["headers"]["x-signature-timestamp"]
        str_body = event['body']
        verify_key.verify(f'{timestamp}{str_body}'.encode(), bytes.fromhex(signature))
        return None
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

def ping_response():
    print('found a ping. responding with "type": 1')
    return {
        'statusCode': 200,
        'body': json.dumps({"type": 1})
    }

def handle_command(body):
    print(json.dumps(body))
    command = DiscordCommand(body)
    if not command.verify_user():
        return command.respond(f"I'm sorry <@{command.user_id}>, I'm afraid I can't do that. (You don't have the right role.)")
    if command.name == "link":
        # TODO send message to api server here (include token)
        return command.respond(f"<@{command.user_id}> your request has been entered. Linking your accounts now...")
    if command.name == "sync":
        requests.put(f"https://online.arisia.org/api/discord/sync/{command.target_user}", headers=get_api_headers())
        return command.respond("sync worked!")
    if command.name == "saferspace":
        channel_id = SAFER_SPACE_CHANNELS[command.options["channel"]]
        if command.verify_arisian():
            DiscordApiSetUserPermission(command.target_user, channel_id).send()
            return command.respond()
        return command.respond(f"I'm sorry <@{command.user_id}>, I'm afraid I can't do that. (You don't have the right role.)")

def discord_handler(event, context):
    body = json.loads(event['body'])

    has_bad_signature = bad_signature(event)
    if has_bad_signature:
        return has_bad_signature

    print("body type is: {type}".format(**body))
    if body['type'] == 1:
        return ping_response()
    if body['type'] == 2:
        return handle_command(body)


# currently this is VERY VERBOSE we'll need to tone this down for real life
def lambda_handler(event, context): 
    print(json.dumps(event))
    if (event["headers"]["user-agent"] == "Discord-Interactions/1.0 (+https://discord.com)"):
        return discord_handler(event, context)
    return {
        'statusCode': 200,
        'body': "You called the lambda from not discord!"
    }

class DiscordApiMessage:
    def __init__(self, url, method = "POST", payload = None):
        self.headers = {"Authorization": f"Bot {DISCORD_BOT_TOKEN}"}
        self.url = url
        self.payload = payload
        self.method = method

    def send(self):
        self.response = requests.request(self.method, self.url, headers=self.headers, json=self.payload)

class DiscordApiGetUserRoles(DiscordApiMessage):
    def __init__(self, user_id):
        url = f"{BASE_API_URL}/{GUILD_FRAGMENT}/members/{user_id}"
        super().__init__(url, "GET")

    def send(self):
        super().send()
        if self.response.ok:
            return self.response.json()["roles"]
        return None

class DiscordApiSetUserPermission(DiscordApiMessage):
    def __init__(self, user_id, channel_id):
        url = f"{BASE_API_URL}/channels/{channel_id}/permissions/{user_id}"
        payload = { "type": 1, "allow": "84992"}
        super().__init__(url, "PUT", payload)

class DiscordCommand:
    def __init__(self, payload):
        self.name = payload["data"]["name"]
        self.options = {}
        if "options" in payload["data"]:
            for option in payload["data"]["options"]:
                self.options[option["name"]] = option["value"]
        self.user_id = payload["member"]["user"]["id"]
        self.user_roles = payload["member"]["roles"]
        self.token = payload["token"]
        if "other-user" in self.options:
            self.target_user = self.options["other-user"]
        else: 
            self.target_user = self.user_id

    def verify_user(self):
        helper_role = os.getenv("DISCORD_HELPER_ROLE_ID")
        print(f"target user: {self.target_user}, user_id: {self.user_id}")
        if self.target_user != self.user_id:
            if helper_role not in self.user_roles:
                return False
        return True

    def verify_arisian(self):
        arisian = os.getenv('DISCORD_ROLE_ID')
        if self.target_user == self.user_id:
            if arisian not in self.user_roles:
                return False
        else:
            self.target_roles = DiscordApiGetUserRoles(self.target_user).send()
            if arisian not in self.target_roles:
                return False
        return True

    def respond(self, message: str = None):
        payload = {
            "type": 3 if message else 2 
        }
        if message:
            payload["data"] = { "content": message}
        return {
            'statusCode': 200,
            'body': json.dumps(payload)
        }
