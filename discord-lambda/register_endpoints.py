import requests
import os
from dotenv import load_dotenv
load_dotenv()

DISCORD_CLIENT_ID = os.getenv('DISCORD_CLIENT_ID')
DISCORD_GUILD_ID = os.getenv('DISCORD_GUILD_ID')
DISCORD_BOT_TOKEN = os.getenv('DISCORD_BOT_TOKEN')

url = f"https://discord.com/api/v8/applications/{DISCORD_CLIENT_ID}/guilds/{DISCORD_GUILD_ID}/commands"

json = {
    "name": "link",
    "description": "link your arisia account with your discord account",
    "options": [
        {
            "name": "badgeid",
            "description": "your 4, 5, or 6 digit arisia badge number",
            "type": 3,
            "required": True
        },
        {
            "name": "secret",
            "description": "a string of gobledegook found on online.arisia.org",
            "type": 3,
            "required": True
        },
        {
            "name": "other-user",
            "description": "the user you are trying to link to",
            "type": 6
        }
    ]
}

# For authorization, you can use either your bot token 
headers = {
    "Authorization": f"Bot {DISCORD_BOT_TOKEN}"
}

r = requests.post(url, headers=headers, json=json)