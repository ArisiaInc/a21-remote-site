import csv,json

def main():
    data = {}
    with open('gaming_11_1.csv', 'r') as f_in:
        r = csv.DictReader(f_in)
        for row in r:
            game_event = {}
            if 'id' not in row:
                continue
            game_event['id'] = row['id']
            if 'link' in row:
                game_event['loc'] = [row['link']]
            data[row['id']] = game_event
    values = [x for x in data.values()]
    with open('gaming_11_1.json', 'w') as f_out:
        json.dump(values, f_out)

if __name__ == "__main__":
    main()
