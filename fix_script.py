# Let's inspect generate_dataset.py lines around 580
with open('generate_dataset.py', 'rb') as f:
    data = f.read()
print("Total bytes:", len(data))
