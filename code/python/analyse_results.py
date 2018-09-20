from enum import Enum
from util import get_files_prefix
import operator
class Category(Enum):
    CATEGORY_BLURRY_NOTHING =           0
    CATEGORY_COLON_CLEAR =              1
    CATEGORY_DYED_LIFTED_POLYPS =       2
    CATEGORY_DYED_RESECTION_MARGINS =   3
    CATEGORY_ESOPHAGITIS =              4
    CATEGORY_INSTRUMENTS =              5
    CATEGORY_NORMAL_CECUM =             6
    CATEGORY_NORMAL_PYLORUS =           7
    CATEGORY_NORMAL_Z_LINE =            8
    CATEGORY_OUT_OF_PATIENT =           9
    CATEGORY_POLYPS =                   10
    CATEGORY_RETROFLEX_RECTUM =         11
    CATEGORY_RETROFLEX_STOMACH =        12
    CATEGORY_STOOL_INCLUSIONS =         13
    CATEGORY_STOOL_PLENTY =             14
    CATEGORY_ULCERATIVE_COLITIS =       15
    
class Result():
    
    def __init__(self,filename):
        self.total = 0
        self.correct = 0
        self.incorrect = 0
        self.correct_percent = 0.0
        self.filename = filename
        w, h = 16, 16
        self.confusion_matrix = [[0 for x in range(w)] for y in range(h)] 
        self.parse()

    def parse(self):
        lines=[]
        with open(self.filename) as f:
            lines = f.readlines()
        self.total =            int(lines[0].strip().split(':')[1])
        self.correct =          int(lines[1].strip().split(':')[1])
        self.incorrect =        int(lines[2].strip().split(':')[1])
        self.correct_percent =  float(lines[3].strip().split(':')[1][:-1])
        idx_w, idx_h = 0,0
        for i in range(6,len(lines)):
            splits = lines[i].split(' ')
            for s in splits:
                if s.strip().isdigit():
                    self.confusion_matrix[idx_h][idx_w] = int(s)
                    idx_w += 1
            idx_h += 1
            idx_w = 0
    
    def print(self):
        print('total:',self.total)
        print('correct:',self.correct)
        print('incorrect:',self.incorrect)
        print('correct Pcnt:',self.correct_percent)
        for h in range(len(self.confusion_matrix)):
            for w in range(len(self.confusion_matrix[h])):
                print('%3s'%self.confusion_matrix[h][w],end=' ')
            print('\n')
    
    def get_filename(self):
        return self.filename
    def get_total(self):
        return self.total
    def get_correct(self):
        return self.correct
    def get_incorrect(self):
        return self.incorrect
    def get_correct_percent(self):
        return self.correct_percent

    def get_worst_category(self):
        max_val = 0
        for i in range(len(self.confusion_matrix)):
            for j in range(len(self.confusion_matrix[i])):
                if i == j:
                    continue
                if max_val < self.confusion_matrix[i][j]:
                    max_val = self.confusion_matrix[i][j]
        res = []        
        for i in range(len(self.confusion_matrix)):
            for j in range(len(self.confusion_matrix[i])):
                if self.confusion_matrix[i][j] == max_val and i != j:
                    res.append([max_val, Category(j), Category(i)])
        return res
    
    def get_matrix_value_at(self,idx_h,idx_w):
        return self.confusion_matrix[idx_h][idx_w]

    def get_correct_pcnt_of_cat(self,cat):
        sum = 0
        for i in range(len(self.confusion_matrix)):
            sum += self.confusion_matrix[i][cat]
        return self.get_matrix_value_at(cat,cat)/float(sum) * 100


filenames = get_files_prefix('/home/michael/master_thesis/data/results','results_')
worst_cat = dict()
max_results = 20
top_filenames = []
top_filenames_ESO_NZL = []

for f in filenames:
    if f.find('MetricSpaces') != -1:
        continue
    if f.find('COSINE') == -1:
        continue
    test = Result(f)
    top_filenames.append((test.get_correct_percent(),f))
    # if len(top_filenames) >= max_results and top_filenames[0][0] <= test.get_correct_percent() :
    #     for i in reversed(range(len(top_filenames)-1)):
    #         top_filenames[i+1] = top_filenames[i]
    #     top_filenames[0] = (test.get_correct_percent(),test.get_filename())
    # elif len(top_filenames) < max_results:
    #     top_filenames.append((test.get_correct_percent(),test.get_filename()))
    # else:
    #     for i in range(len(top_filenames)):
    #         if test.get_correct_percent() > top_filenames[i][0]:
    #             top_filenames.insert(i-1,(test.get_correct_percent(),test.get_filename() ))
    #             top_filenames.pop()

    # if len(top_filenames_ESO_NZL) >= max_results and top_filenames_ESO_NZL[0][0] >= test.get_matrix_value_at(Category.CATEGORY_NORMAL_Z_LINE.value,Category.CATEGORY_ESOPHAGITIS.value) :
    #     for i in reversed(range(len(top_filenames_ESO_NZL)-1)):
    #         top_filenames_ESO_NZL[i+1] = top_filenames_ESO_NZL[i]
    #     top_filenames_ESO_NZL[0] = (test.get_matrix_value_at(Category.CATEGORY_NORMAL_Z_LINE.value,Category.CATEGORY_ESOPHAGITIS.value),test.get_filename())
    # elif len(top_filenames_ESO_NZL) < max_results:
    #     top_filenames_ESO_NZL.append((test.get_matrix_value_at(Category.CATEGORY_NORMAL_Z_LINE.value,Category.CATEGORY_ESOPHAGITIS.value),test.get_filename()))
    # else:
    #     for i in range(len(top_filenames_ESO_NZL)):
    #         if test.get_matrix_value_at(Category.CATEGORY_NORMAL_Z_LINE.value,Category.CATEGORY_ESOPHAGITIS.value) < top_filenames_ESO_NZL[i][0]:
    #             top_filenames_ESO_NZL.insert(i-1,(test.get_matrix_value_at(Category.CATEGORY_NORMAL_Z_LINE.value,Category.CATEGORY_ESOPHAGITIS.value),test.get_filename() ))
    #             top_filenames_ESO_NZL.pop()
    # top_filenames = list(set(top_filenames))
    top_filenames.sort(key=lambda tup: tup[0], reverse=True)
    # top_filenames_ESO_NZL = list(set(top_filenames_ESO_NZL))
    # top_filenames_ESO_NZL.sort(key=lambda tup: tup[0])
    cat_pcnt = []
    print(f, test.get_correct_percent())
    for cat in range(len(Category)):
        cat_pcnt.append((str(Category(cat).name),test.get_correct_pcnt_of_cat(cat)))
    # cat_pcnt.sort(key=lambda tup: tup[1], reverse=True)
    for i in range(len(cat_pcnt)):
        print('{:<2}'.format(i+1)+':','{:<6.2f}'.format(cat_pcnt[i][1]) + "%",cat_pcnt[i][0])
    # print(test.get_filename())
    res = test.get_worst_category()
    # print(res)
    # for l in res:
    #     if (l[1],l[2]) in worst_cat:
    #         worst_cat[(l[1],l[2])] = worst_cat[(l[1],l[2])] + 1
    #     else:
    #         worst_cat[(l[1],l[2])] = 1
       
    
    # print()

for i in range(len(top_filenames)):
    print(i+1,':','{:<5}'.format(top_filenames[i][0]) + '%',top_filenames[i][1])
# print()
# for i in range(len(top_filenames_ESO_NZL)):
#     print(i+1,':','{:<5}'.format(top_filenames_ESO_NZL[i][0]),top_filenames_ESO_NZL[i][1])
# print()
# if len(worst_cat) != 0:
#     print('1 worst cat:',max(worst_cat.items(),key= operator.itemgetter(1)))
#     for i in range(len(worst_cat)):
#         del worst_cat[max(worst_cat.items(),key= operator.itemgetter(1))[0]]
#         if len(worst_cat) == 0:
#             break
#         print(i+2,'worst cat:',max(worst_cat.items(),key= operator.itemgetter(1)))