import numpy as np
import timeit
from util import get_files
from keras.preprocessing import image
import keras.applications as appl
from keras.models import Model
from keras.layers import  GlobalAveragePooling2D,GlobalMaxPooling2D ,Dense
filesdir = '/home/mst/master_thesis/data/Medico_2018_development_set/Medico_2018_development_set/'
csv_folder = '/home/mst/master_thesis/code/python/csv/lessLayers/'
def extract_features_to_csv_string(img_path,model,feature_reshape_param,prep_input, model_targetsize):
   
    img = image.load_img(img_path, target_size=model_targetsize)
    
    x = image.img_to_array(img)
    x = np.expand_dims(x, axis=0)
    x = prep_input(x)
    features = model.predict(x)
    features = features.reshape(feature_reshape_param)
    text = img_path + ','
    for x in features:
        for y in x:
            text = text + "{:.40f}".format(y).rstrip('0').rstrip('.')+ ','
    text = text + '\n'
    return text
    

def modelFromLayer(model, layer_name):
    x = model.get_layer(layer_name)
    x = Dense(1024)(x)
    x = GlobalAveragePooling2D(name='gavgpool')(x)
    model = Model(model.input,x)
    return model

def addLayer(model):
    x = model.output
    x = Dense(1024)(x)
    x = GlobalAveragePooling2D(name='gavgpool')(x)
    model = Model(model.input,x)
    return model

# create models
if __name__ == '__main__':
    files = get_files(filesdir)
    start = timeit.default_timer()
    models = dict()
    print('setting up models...')
    model = appl.xception.Xception(weights='imagenet', include_top=False)
    model =  modelFromLayer(model,'block4_pool')
    models['xception'] =    model
    model = appl.vgg16.VGG16(weights='imagenet', include_top=False)
    model = modelFromLayer(model,'block4_pool')
    models['vgg16'] =       model
    model = appl.vgg19.VGG19(weights='imagenet', include_top=False)
    model = modelFromLayer(model,'block4_pool')
    models['vgg19'] =       model
    model = appl.resnet50.ResNet50(weights='imagenet', include_top=False)
    model = modelFromLayer(model,'bn4a_branch1')
    models['resnet50'] =    model
    model = appl.inception_v3.InceptionV3(weights='imagenet', include_top=False)
    model = modelFromLayer(model,'mixed9_1')
    models['inceptionv3'] = model
    model = appl.inception_resnet_v2.InceptionResNetV2(weights='imagenet', include_top = False)
    model = modelFromLayer(model,'mixed_6a')
    models['incresnetv2'] = model
    model = appl.mobilenet.MobileNet(weights='imagenet', include_top=False)
    model = modelFromLayer(model,'conv_pw_5_relu')
    models['mobilenet'] =   model
    model = appl.densenet.DenseNet121(weights='imagenet' ,include_top = False)
    model = modelFromLayer(model,'pool4')
    models['densenet121'] = model
    model = appl.densenet.DenseNet169(weights='imagenet', include_top=False)
    model = modelFromLayer(model,'pool4')
    models['densenet169'] = model
    model = appl.densenet.DenseNet201(weights='imagenet', include_top=False)
    model = modelFromLayer(model,'pool4')
    models['densenet201'] = model
    stop = timeit.default_timer()
    print('setting up models took',stop-start,'sec')
    #prep_functions
    start = timeit.default_timer()
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
    #csv filenames
    print('setting up csv_filenames...')
    csv_filenames = dict()
    for k in models.keys():
        csv_filenames[k] = csv_folder + k + '.csv'
    stop = timeit.default_timer()
    print('setting up other stuff took',stop-start,'sec')
    start = timeit.default_timer()
    for k in models.keys():
        s= timeit.default_timer()
        print("%12s : %s"%(k,models[k].output_shape))
        lines = []
        i = 1
        for f in files:
            lines.append(extract_features_to_csv_string(f, models[k],(1024,1),prep_funs[k],inputsizes[k]))
            print('processed', "{:.2f}".format(i/len(files)*100),'%',end='\r')
            i += 1
        with open(csv_filenames[k],'a+') as csvfile:
            csvfile.writelines(lines)
        e = timeit.default_timer()
        print('\nprocessed model:',k,e-s,'sec')
    stop = timeit.default_timer()
    print('processing models took',stop-start,'sec')

