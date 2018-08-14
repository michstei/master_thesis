import os


def get_files(dir):
    files = []
    # r=root, d=directories, f = files
    for r, d, f in os.walk(dir):
        for file in f:
            if ".jpg" in file:
                files.append(os.path.join(r, file))
    return files