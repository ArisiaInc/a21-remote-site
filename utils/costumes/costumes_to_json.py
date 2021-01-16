import csv,json
import sys
import os
from pathlib import Path

image_required_fields = ['creator_id', 'url', 'alt', 'title', 'image_id']

image_fields_to_keep = ['creator_id', 'url', 'alt' , 'title', 'description' ,'image_id']

def image_has_required_fields(row):
    return all(map(lambda x: x in row, image_required_fields))

def run_all():
    """runs both artists and dealers"""
    paths = sorted(Path(__file__).parent.iterdir(), key=os.path.getmtime) 
    costume_image_fname = None
    for path in paths: 
        fname = path.parts[-1]
        if not fname.endswith('.csv'):
            continue
        if fname.startswith('costumes'):
            costume_image_fname = fname
            continue
    if costume_image_fname:
        main(None, costume_image_fname)

def main(*args):

    fname = args[1]
    if fname.endswith('.csv'):
        fname = fname[:-4]

    data = []

    with open(fname + ".csv", 'r') as f:
        r = csv.DictReader(f)
        for row in r:
            # using a list comprehension instead of an iterator so i can modify as i go
            for key in [x for x in row.keys()]:
                # get rid of unwanted fields
                if key not in image_fields_to_keep:
                    del row[key]
                    continue
                # delete missing fields
                if row[key] == '':
                    del row[key]
                    continue
            # check for url
            if 'url' not in row or not row['url'].startswith('https'):
                # needs a valid url
                print('Image {} is missing a valid url.'.format(row['image_id']))
                row['url'] = 'assets/images/giant_duck.jpg'
            if 'description' in row and 'alt' not in row:
                # use description as alt text
                row['alt'] = row['description']
            # check for required fields
            if not image_has_required_fields(row):
                print('Image {} is missing one or more of the required fields: {}'.format(row['image_id'], ', '.join(image_required_fields)))
            data.append(row)

    with open(fname + '.json', 'w') as f_out:
        json.dump(data, f_out)

if __name__ == "__main__":
    if len(sys.argv) == 1:
        run_all()
        sys.exit(0)
    if len(sys.argv) > 3:
        'usage: python costumes_to_json.py costumes_xxxx.csv'
        sys.exit(1)
    main(sys.argv)
