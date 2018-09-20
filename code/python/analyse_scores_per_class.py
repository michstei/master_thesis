base_path = "/home/michael/master_thesis/data/results/kg/results_scores_per_class.txt"

categories = ['blurry-nothing','colon-clear','dyed-lifted-polyps','dyed-resection-margins','esophagitis','instruments','normal-cecum','normal-pylorus','normal-z-line','out-of-patient','polyps','retroflex-rectum','retroflex-stomach','stool-inclusions','stool-plenty','ulcerative-colitis']
class_names = ['DenseNet121_Int','DenseNet169_Int','DenseNet201_Int','ResNet50_Int','MobileNet_Int','VGG16_Int','VGG19_Int','Xception_Int','ACCID','ColorLayout']

class Record():
    def __init__(self):
        self.filename = ''
        self.category = ''
        self.corrects = dict()
        self.incorrects = dict()
        self.total = dict()
            

lines = []
with open(base_path) as f:
    lines = f.readlines()
records = []
current_record = None
current_class_name = ''
for line in lines:
    if line.startswith('/home'):
        if current_record is not None:
            records.append(current_record)
        current_record = Record()
        current_record.filename = line.split(':')[0]
        for cat in categories:
            if '/' + cat + '/' in current_record.filename:
                current_record.category = cat 
        continue
    if line.split(':')[0] in class_names:
        current_class_name = line.split(':')[0]
        if current_class_name not in current_record.total:
            current_record.total[current_class_name] = 0
        if current_class_name not in current_record.corrects:
            current_record.corrects[current_class_name] = 0
        if current_class_name not in current_record.incorrects:
            current_record.incorrects[current_class_name] = 0
        continue
    if line[0].isdigit():
        current_record.total[current_class_name] = current_record.total[current_class_name] + 1
        if '/' + current_record.category + '/' not in line:
            if current_class_name not in current_record.incorrects:
                current_record.incorrects[current_class_name] = 1
            else:
                current_record.incorrects[current_class_name] = current_record.incorrects[current_class_name] + 1
        else:
            if current_class_name not in current_record.corrects:
                current_record.corrects[current_class_name] = 1
            else:
                current_record.corrects[current_class_name] = current_record.corrects[current_class_name] + 1

results = dict()
totals = dict()
for record in records:
    if record.category not in results:
        results[record.category] = dict()
        totals[record.category] = dict()
    for key in record.total:
        total = record.total[key]
        correct = record.corrects[key]
        incorrect = record.incorrects[key]
        if key not in results[record.category]:
            results[record.category][key] = 0
        if key not in totals[record.category]:
            totals[record.category][key] = 0
        results[record.category][key] = results[record.category][key] + correct
        totals[record.category][key] = totals[record.category][key] + total
for r in results:
    for c in results[r]:
        results[r][c] = results[r][c] / totals[r][c] * 100

for r in results:
    print(r)
    for c in results[r]:
        print('%s %15s %s %2.2f'%('\t',c,':',results[r][c]))