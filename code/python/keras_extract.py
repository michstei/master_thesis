import numpy as np
import timeit
from util import get_files
from keras.preprocessing import image
import keras.applications as appl
from keras.models import Model
filesdir = '/home/michael/master_thesis/data/Medico_2018_development_set/'
csv_folder = '/home/michael/master_thesis/data/csv/'
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
    
# create models
if __name__ == '__main__':
    files = get_files(filesdir)
    start = timeit.default_timer()
    models = dict()
    print('setting up models...')
    # models['xception'] =    appl.xception.Xception(                     weights="imagenet", include_top=False,pooling='avg')
    # models['vgg16'] =       appl.vgg16.VGG16(                           weights="imagenet", include_top=False,pooling='avg')
    # models['vgg19'] =       appl.vgg19.VGG19(                           weights="imagenet", include_top=False,pooling='avg')
    # models['resnet50'] =    appl.resnet50.ResNet50(                     weights="imagenet", include_top=False,pooling='avg')
    models['inceptionv3'] = appl.inception_v3.InceptionV3(              weights="imagenet", include_top=False,pooling='avg')
    models['incresnetv2'] = appl.inception_resnet_v2.InceptionResNetV2( weights="imagenet", include_top=False,pooling='avg')
    models['mobilenet'] =   appl.mobilenet.MobileNet(                   weights="imagenet", include_top=False,pooling='avg')
    models['densenet121'] = appl.densenet.DenseNet121(                  weights="imagenet", include_top=False,pooling='avg')
    models['densenet169'] = appl.densenet.DenseNet169(                  weights="imagenet", include_top=False,pooling='avg')
    models['densenet201'] = appl.densenet.DenseNet201(                  weights="imagenet", include_top=False,pooling='avg')
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
    #reshape params
    print('setting up reshape params...')
    reshape_params = dict()
    reshape_params['xception'] =    (2048, 1)
    reshape_params['vgg16'] =       (512 , 1)
    reshape_params['vgg19'] =       (512 , 1)
    reshape_params['resnet50'] =    (2048, 1)
    reshape_params['inceptionv3'] = (2048, 1)
    reshape_params['incresnetv2'] = (1536, 1)
    reshape_params['mobilenet'] =   (1024, 1)
    reshape_params['densenet121'] = (1024, 1)
    reshape_params['densenet169'] = (1664, 1)
    reshape_params['densenet201'] = (1920, 1)
    #csv filenames
    print('setting up csv_filenames...')
    csv_filenames = dict()
    for k in models.keys():
        csv_filenames[k] = csv_folder + k + '.csv'
    stop = timeit.default_timer()
    print('setting up other stuff took',stop-start,'sec')
    start = timeit.default_timer()
    for k in models.keys():
        print(models[k].output_shape)
        lines = []
        i = 1
        for f in files:
            lines.append(extract_features_to_csv_string(f, models[k],reshape_params[k],prep_funs[k],inputsizes[k]))
            print('processed', "{:.2f}".format(i/len(files)*100),'%',end='\r')
            i += 1

        with open(csv_filenames[k],'a') as csvfile:
            csvfile.writelines(lines)
        print('\nprocessed model:',k)
    stop = timeit.default_timer()
    print('processing models took',stop-start,'sec')

