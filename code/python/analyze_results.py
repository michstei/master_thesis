import os
import collections 
base_paths = ["/home/michael/master_thesis/data/results/MetricSpaces/doubleFeatureVectors/",
"/home/michael/master_thesis/data/results/MetricSpaces/floatFeatureVectors/",
"/home/michael/master_thesis/data/results/MetricSpaces/longFeatureVectors/",
"/home/michael/master_thesis/data/results/MetricSpaces/intFeatureVectors/",
"/home/michael/master_thesis/data/results/MetricSpaces/shortFeatureVectors/",
"/home/michael/master_thesis/data/results/MetricSpaces/byteFeatureVectors/"]

def list_files(path, postfix, l):
    for file in os.listdir(path):
        if file.endswith(postfix):
            l.append(os.path.join(path, file))
            # print(os.path.join(path, file))
        else:
            if(os.path.isdir(os.path.join(path, file))):
                list_files(os.path.join(path, file),postfix,l)
worst = dict()

for base_path in base_paths:
    files = []
    list_files(base_path,"best_result_per_feature.txt",files)

    false_predictions = dict()
    true_predictions = dict()

    for f in files:
        lines = []
        with open(f) as fi:
            lines = fi.readlines()
        
        for line in lines:
            if line.startswith('/'):
                continue
            splits1 = line.split(':')
            if 'false' in splits1[0]:
                key = splits1[0].split(' ')[0].strip().split('_')[0]
                if key in false_predictions:
                    false_predictions[key] = false_predictions[key] + 1
                else:
                    false_predictions[key] = 1
            else:
                key = splits1[0].split(' ')[0].strip().split('_')[0]
                if key in true_predictions:
                    true_predictions[key] = true_predictions[key] + 1
                else:
                    true_predictions[key] = 1
        
    print(base_path)
    
    for key in false_predictions:
        print("%-12s %s %s %s %s %s %s %s %s" % (key, "true:", true_predictions[key], "false:", false_predictions[key], "sum:",true_predictions[key] + false_predictions[key], "pcnt:", (true_predictions[key]/(true_predictions[key]+false_predictions[key]))*100 ))
    
    m = max(false_predictions.values())
    for key in false_predictions:
        if false_predictions[key] == m:
            if key not in worst:
                worst[key] = 1
            else:
                worst[key] = worst[key] + 1
            print(key,":",m)
    print()
print(worst)