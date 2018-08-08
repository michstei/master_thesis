from keras.applications.vgg16 import VGG16
from keras.applications.vgg19 import VGG19
from keras.preprocessing import image
import keras.applications.vgg16 as vg16
import keras.applications.vgg19 as vg19
from keras.models import Model
import numpy as np
import matplotlib.pyplot as plt
import csv
img_path = '../../data/Medico_2018_development_set/colon-clear/1.jpg'
img_path2 = '../../data/Medico_2018_development_set/colon-clear/2.jpg'

def extract_features(img_path,model,layer,csv_file,feature_reshape_param,prep_input):
    img = image.load_img(img_path, target_size=(224, 224))
    
    x = image.img_to_array(img)
    x = np.expand_dims(x, axis=0)
    x = prep_input(x)
    model.predict(x)
    model_extractfeatures = Model(input=model.input, output=model.get_layer(layer).output)
    fc2_features = model_extractfeatures.predict(x)
    fc2_features = fc2_features.reshape(feature_reshape_param)
    text = img_path + ','
    for x in fc2_features:
        for y in x:
            text = text + str(y) + ','
    text = text + '\n'
    with open(csv_file,'a') as csvfile:
        csvfile.write(text)
# plt.plot(fc2_features)
# plt.show()
model = VGG16(weights='imagenet', include_top=True)
model2 = VGG19(weights='imagenet', include_top=True)
extract_features(img_path,model,'fc2','fc2_vgg16.csv',(4096,1),vg16.preprocess_input)
extract_features(img_path2,model,'fc2','fc2_vgg16.csv',(4096,1),vg16.preprocess_input)
extract_features(img_path,model2,'fc2','fc2_vgg19.csv',(4096,1),vg19.preprocess_input)
extract_features(img_path2,model2,'fc2','fc2_vgg19.csv',(4096,1),vg19.preprocess_input)