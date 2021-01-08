import csv,json
import sys
import os
from pathlib import Path

required_fields = ['id', 'name', 'summary', 'images', 'order']
image_required_fields = ['creator_id', 'url', 'alt', 'title', 'image_id']

fields_to_keep = ['id','name','summary','links__web','links__facebook','links__etsy','links__insta','links__youtube','links__other','links__preferred','description']
image_fields_to_keep = ['creator_id', 'url', 'alt' , 'title', 'description' ,'image_id']

def has_required_fields(row):
    return all(map(lambda x: x in row, required_fields))

def image_has_required_fields(row):
    return all(map(lambda x: x in row, image_required_fields))

def run_all():
    """runs both artists and dealers"""
    paths = sorted(Path(__file__).parent.iterdir(), key=os.path.getmtime) 
    dealer_image_fname = None
    dealer_fname = None
    artist_image_fname = None
    artist_fname = None
    for path in paths: 
        fname = path.parts[-1]
        if not fname.endswith('.csv'):
            continue
        if fname.startswith('dealer_images'):
            dealer_image_fname = fname
            continue
        if fname.startswith('dealers'):
            dealer_fname = fname
            continue
        if fname.startswith('artist_images'):
            artist_image_fname = fname
            continue
        if fname.startswith('artists'):
            artist_fname = fname
            continue
    if dealer_fname and dealer_image_fname:
        main(None, dealer_fname, dealer_image_fname)
    if artist_fname and artist_image_fname:
        main(None, artist_fname, artist_image_fname)

def main(*args):

    fname = args[1]
    if fname.endswith('.csv'):
        fname = fname[:-4]
    if len(args) > 2:
        image_fname = args[2]
    else:
        image_fname = fname.replace('s', '_images')
        image_fname += ".csv"

    data = {}
    order = 0

    with open(fname + '.csv', 'r') as f_in:
            r = csv.DictReader(f_in)
            for row in r:
                # should probably not be modifying read results in place. but...
                # using a list comprehension instead of an iterator so i can modify as i go
                for key in [x for x in row.keys()]:
                    # get rid of extraneous fields
                    if key not in fields_to_keep:
                        del row[key]
                        continue
                    # do not want empty fields
                    if row[key] == "":
                        del row[key]
                        continue
                    if "__" in key:
                        tokens = key.split("__")
                        if tokens[0] not in row:
                            row[tokens[0]] = {}
                        row[tokens[0]][tokens[1]] = row[key]
                        del row[key]
                row["images"] = []
                row["order"] = order
                order += 1
                # check to make sure all required fields are there
                if not has_required_fields(row):
                    print('Dealer {} is missing some of the required fields: {}'.format(row['name'], ', '.join(required_fields)))
                data[row['id']] = row

    with open(image_fname, 'r') as f:
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
            # check for image id
            if 'image_id' not in row:
                print('Missing image id: {}'.format(row))
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
            data[row['creator_id']]['images'].append(row)

    values = sorted(data.values(), key=lambda x: x['order'])
    for creator in values:
        # put in dummy image
        if len(creator['images']) == 0:
            creator['images'].append({
                'creator_id': creator['id'],
                'alt': 'a giant inflatable duck in the singapore harbor',
                'url': 'assets/images/giant_duck.jpg',
                'title': 'I did not yet submit images',
                'image_id': '0'
            })
    with open(fname + '.json', 'w') as f_out:
        json.dump(values, f_out)

if __name__ == "__main__":
    if len(sys.argv) == 1:
        run_all()
        sys.exit(0)
    if len(sys.argv) > 3:
        'usage: python creators_to_json.py creators_xxxx.csv creator_images_xxxx.csv'
        sys.exit(1)
    main(sys.argv)
