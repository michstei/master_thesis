
from keras.preprocessing import image
import keras.applications as appl
from keras.models import Model
import numpy as np
import matplotlib.pyplot as plt
import csv
import timeit
from keras import backend as be
img_path = '../../data/Medico_2018_development_set/colon-clear/1.jpg'
img_path2 = '../../data/Medico_2018_development_set/colon-clear/2.jpg'




def extract_features(img_path,model,layer,csv_file,feature_reshape_param,prep_input, model_targetsize):
    img = image.load_img(img_path, target_size=model_targetsize)
    
    x = image.img_to_array(img)
    x = np.expand_dims(x, axis=0)
    x = prep_input(x)
    model.predict(x)
    model_extractfeatures = Model(input=model.input, output=model.get_layer(layer).output)
    features = model_extractfeatures.predict(x)
    
    features = features.reshape(feature_reshape_param)
    text = img_path + ','
    for x in features:
        for y in x:
            text = text + str(y) + ','
    text = text + '\n'
    with open(csv_file,'a') as csvfile:
        csvfile.write(text)
# plt.plot(features)
# plt.show()
# create models
be.clear_session()
start = timeit.default_timer()


models = dict()
print('setting up models...')
models['xception'] =    appl.xception.Xception(weights='imagenet', include_top=True)
print('setup model','xception','done')
models['vgg16'] =       appl.vgg16.VGG16(weights='imagenet', include_top=True)
print('setup model','vgg16','done')
models['vgg19'] =       appl.vgg19.VGG19(weights='imagenet', include_top=True)
print('setup model','vgg19','done')
models['resnet50'] =    appl.resnet50.ResNet50(weights='imagenet',include_top=True)
print('setup model','resnet50','done')
models['inceptionv3'] = appl.inception_v3.InceptionV3(weights='imagenet',include_top=True)
print('setup model','inceptionv3','done')
models['incresnetv2'] = appl.inception_resnet_v2.InceptionResNetV2(weights='imagenet',include_top=True)
print('setup model','incresnetv2','done')
models['mobilenet'] =   appl.mobilenet.MobileNet(weights='imagenet',include_top=True)
print('setup model','mobilenet','done')
models['densenet121'] = appl.densenet.DenseNet121(weights='imagenet',include_top=True)
print('setup model','densenet121','done')
models['densenet169'] = appl.densenet.DenseNet169(weights='imagenet',include_top=True)
print('setup model','densenet169','done')
models['densenet201'] = appl.densenet.DenseNet201(weights='imagenet',include_top=True)
print('setup model','densenet201','done')
print('all models set up')
stop = timeit.default_timer()
print('setting up models took',stop-start,'sec')
#prep layers to extract
start = timeit.default_timer()
print('setting up layers to extract')
layers = dict()
layers['xception'] =    'predictions'
layers['vgg16'] =       'fc2'
layers['vgg19'] =       'fc2'
layers['resnet50'] =    'fc1000'
layers['inceptionv3'] = 'predictions'
layers['incresnetv2'] = 'predictions'
layers['mobilenet'] =   'reshape_2'
layers['densenet121'] = 'fc1000'
layers['densenet169'] = 'fc1000'
layers['densenet201'] = 'fc1000'
print('all layers set up')
#prep_functions
print('setting up prepfunctions...')
prep_funs = dict()
prep_funs['xception'] =    appl.xception.preprocess_input
prep_funs['vgg16'] =       appl.vgg16.preprocess_input
prep_funs['vgg19'] =       appl.vgg19.preprocess_input
prep_funs['resnet50'] =    appl.resnet50.preprocess_input
prep_funs['inceptionv3'] = appl.inception_v3.preprocess_input
prep_funs['incresnetv2'] = appl.inception_resnet_v2.preprocess_input
prep_funs['mobilenet'] =   appl.mobilenet.preprocess_input
prep_funs['densenet121'] = appl.densenet.preprocess_input
prep_funs['densenet169'] = appl.densenet.preprocess_input
prep_funs['densenet201'] = appl.densenet.preprocess_input
print('all prepfunctions set up')
#inputsizes
print('setting up inputsizes...')
inputsizes = dict()
inputsizes['xception'] =    (229,229)
inputsizes['vgg16'] =       (224,224)
inputsizes['vgg19'] =       (224,224)
inputsizes['resnet50'] =    (224,224)
inputsizes['inceptionv3'] = (229,229)
inputsizes['incresnetv2'] = (229,229)
inputsizes['mobilenet'] =   (224,224)
inputsizes['densenet121'] = (224,224)
inputsizes['densenet169'] = (224,224)
inputsizes['densenet201'] = (224,224)
print('all inputsizes set up')
#inputsizes
print('setting up reshape params...')
reshape_params = dict()
reshape_params['xception'] =    (1000,1)
reshape_params['vgg16'] =       (4096,1)
reshape_params['vgg19'] =       (4096,1)
reshape_params['resnet50'] =    (1000,1)
reshape_params['inceptionv3'] = (1000,1)
reshape_params['incresnetv2'] = (1000,1)
reshape_params['mobilenet'] =   (1000,1)
reshape_params['densenet121'] = (1000,1)
reshape_params['densenet169'] = (1000,1)
reshape_params['densenet201'] = (1000,1)
print('all reshape params set up')
#csv filenames
print('setting up csv_filenames...')
csv_filenames = dict()
for k in models.keys():
    csv_filenames[k] = 'csv_' + k + '.csv'
print('all csv_filenames set up')
stop = timeit.default_timer()
print('setting up other stuff took',stop-start,'sec')
start = timeit.default_timer()
for k in models.keys():
    print(models[k].get_layer(layers[k]).output_shape)
    extract_features(img_path,  models[k],layers[k],csv_filenames[k],reshape_params[k],prep_funs[k],inputsizes[k])
    # extract_features(img_path2, models[k],layers[k],csv_filenames[k],reshape_params[k],prep_funs[k],inputsizes[k])
    print('processed model:',k)
stop = timeit.default_timer()
print('processing models took',stop-start,'sec')

