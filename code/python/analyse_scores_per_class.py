
base_path = "/home/michael/master_thesis/data/results/kgcw/"
filename = "/results_findings_per_class.txt"
range_ = range(1,21)
categories = ['blurry-nothing','colon-clear','dyed-lifted-polyps','dyed-resection-margins','esophagitis','instruments','normal-cecum','normal-pylorus','normal-z-line','out-of-patient','polyps','retroflex-rectum','retroflex-stomach','stool-inclusions','stool-plenty','ulcerative-colitis']
class_names = ['DenseNet121_Int','DenseNet169_Int','DenseNet201_Int','ResNet50_Int','MobileNet_Int','VGG16_Int','VGG19_Int','Xception_Int']

class Record():
    def __init__(self):
        self.filename = ''
        self.category = ''
        self.counter = dict()
        self.corrects = dict()
        self.incorrects = dict()
        self.total = dict()

records = dict()         
for path_idx in range_:
    lines = []
    with open(base_path + str(path_idx) + filename) as f:
        lines = f.readlines()
   
    records[path_idx] = []
    current_record = None
    current_class_name = ''

    for line in lines:
        # print(line)
        if line.startswith('/home'):
            if current_record is not None:
                records[path_idx].append(current_record)
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
            if current_class_name not in current_record.counter:
                current_record.counter[current_class_name] = dict()
            continue
        if line[0].isdigit():
            found_category = None
            for cat in categories:
                if '/' + cat + '/' in line:
                    found_category = cat
                    break
            if found_category not in current_record.counter[current_class_name]:
                current_record.counter[current_class_name][found_category] = 1
            else:
                current_record.counter[current_class_name][found_category] = current_record.counter[current_class_name][found_category] + 1
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


for cat in categories:
    print(cat)
    wrong_class = dict()
    for path_idx in range_:
        wrong_class[path_idx] = dict()
        for record in records[path_idx]:
            if record.category == cat:
                i = 1
                for key in record.counter:
                    if key not in wrong_class[path_idx]:
                        wrong_class[path_idx][key] = 0
                    key_max = []
                    val_max = 0
                    for key2 in record.counter[key]:
                        if val_max < record.counter[key][key2]:
                            val_max = record.counter[key][key2]
                            key_max = [key2]
                        elif val_max == record.counter[key][key2]:
                            key_max.append(key2)
                    # print("%2d : %15s : %r"%(i,key,cat in key_max and len(key_max) == 1),key_max)
                    i += 1
                    if cat not in key_max or len(key_max) != 1:
                        wrong_class[path_idx][key] = wrong_class[path_idx][key] + 1
    for k in class_names:
        sum = 0
        print("\t%15s : "%k,end=" ")
        for path_idx in range_:
            print("%2d"%(wrong_class[path_idx][k]),end=" ")
            sum += wrong_class[path_idx][k]
        print("avg: %2.1f"%(sum/float(len(wrong_class))),end="\n")
        