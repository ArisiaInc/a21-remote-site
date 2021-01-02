# Script to munge creator json files

1. Use the template files (`.csv.template`) to figure out what fields you need
2. Save the data files with the following data scheme:
    2.1. `dealers_<id>.csv` and `dealer_images_<id>.csv`
    2.2. `artists_<id>.csv` and `artist_images_<id>.csv`
3. `python creators_to_json.py`
4. there will be a `dealers_<id>.json` and an `artists_<id>.json` file (or if you didn't name the base files correctly, nothing). it can handle just dealers or just artists. if there's more than one csv file in the directory, it will use the newest.
5. if you want to use this data locally, move it into `frontend/src/assets/data/dealers_<id>.json` (or artists), and change the creatorService to pick up that file.
