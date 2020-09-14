template_service
===
This repo is used for develop app based on osdt mep
-----
This is an app application for face recognition microservices.The service was developed
on the basis of an open project for face recognition on github.The project is characterized
by the following：

Recognize and manipulate faces from Python or from the command line with the world's simplest face recognition library.

Built using dlib's state-of-the-art face recognition built with deep learning. The model has an accuracy of 99.38% on
the Labeled Faces in the Wild benchmark.

This also provides a simple template command line tool that lets you do face recognition on a folder of images
from the command line!

The face recognition microservices have the following features:face upload, face compare，face recognition, Video face recognition.

Data storage using redis and postgresql  

Features
===
face upload
------
Import image files with only one face into the redis database and store them in postgresql<br>
restful API ：POST http://127.0.0.1:9999/v1/face-recognition/upload<br>
Support multiple image files to be simultaneously upload<br>
input：<br>
curl --request POST \
  --url http://127.0.0.1:9999/v1/template/upload \
  --header 'cache-control: no-cache' \
  --header 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  --form file=@picture.jpg<br>
  
face compare
-------
Import two pictures with faces for comparison <br>
restful API ：POST http://127.0.0.1:9999/v1/face-recognition/comparsion<br>
Support multiple faces for comparison<br>
input：<br>
curl --request POST \
  --url http://127.0.0.1:9999/v1/face-recognition/comparsion \
  --header 'cache-control: no-cache' \
  --header 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  --form file1=@picture1.jpg \
  --form file2=@picture2.jpg<br>

face recognition
------
Enter an image file with an unknown name and match the corresponding person name in the redis database<br>
restful API ：POST http://127.0.0.1:9999/v1/face-recognition/recognition<br>
There can be multiple faces in the input image<br>
input：<br>
curl --request POST \
  --url http://127.0.0.1:9999/v1/face-recognition/recognition \
  --header 'cache-control: no-cache' \
  --header 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  --form file=@picture.jpg<br>

face collection
--------
Get the face image from the camera, url is camera IP<br>
restful API ：POST http://127.0.0.1:9999/v1/face-recognition/collection<br>
body: {"url":"192.168.15.120"}<br>

Video face recognition
--------
Get the face video from the camera and find the name of the face in the database.url is camera IP<br>
restful API ：POST http://127.0.0.1:9999/v1/face-recognition/video<br>
body: {"url":"192.168.15.120"}<br>

Installation
===
Requirements
-----
Python 3.6  redis dlib template<br>
docker docker-compose<br>
macOS or Linux (Windows not officially supported, but might work)<br>
Installation Options:(ubuntu18.04)<br>
Third party library:cmake dlib template flask redis opencv-python requests psycopg2-binary

Build
-----
Build a docker image:docker build . -t template:v1.4<br>
docker tag template:v1.4 159.138.11.6:8089/template:v1.4<br>
docker push 159.138.11.6:8089/face-recognition:v1.4<br>
# Publish the docker image to dockergub<br>
test :python3 test.py

Deployment
------
1.packagechart
helm install face-recognition 
2.deployment
kubectl apply -f template_v1.4.yaml

Python Module
----
You can import the template module and then easily manipulate faces with just a couple of lines of code.<br>
API Docs: https://face-recognition.readthedocs.io.


