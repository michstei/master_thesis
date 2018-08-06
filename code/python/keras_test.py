from keras.applications.vgg16 import VGG16
from keras.preprocessing import image
from keras.applications.vgg16 import preprocess_input, decode_predictions
from keras.models import Model
import numpy as np
import os
model = VGG16(weights='imagenet')
layer_name = 'predictions'
intermediate_model = Model(inputs=model.input, outputs=model.get_layer(layer_name).output )
img_path = '../../data/Medico_2018_development_set/colon-clear/1.jpg'
img = image.load_img(img_path, target_size=(224, 224))
x = image.img_to_array(img)
x = np.expand_dims(x, axis=0)
x = preprocess_input(x)

# preds = model.predict(x)

# print('predicted: ', decode_predictions(preds,top=3)[0])
preds = intermediate_model.predict(x)
for x1 in preds:
    # for x2 in x1:
    #     for x3 in x2:
    #         print('x3',len(x3))
    #         break
    #     print('x2',len(x2))
    #     break
    print('x1',len(x1))
    break
print('preds',len(preds))
