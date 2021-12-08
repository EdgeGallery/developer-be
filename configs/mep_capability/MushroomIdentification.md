# 蘑菇类型识别

## 1 介绍

本APP可以根据蘑菇图片中的信息，对其进行类型检测。

本APP模型为[Tensorflow Lite模型](https://hub.tensorflow.google.cn/bohemian-visual-recognition-alliance/models/mushroom-identification_v1/2)，
来源于[TensorFlow Hub开源模型库](https://hub.tensorflow.google.cn/)。

本模型可以对1395种类型的农作物病虫害进行检测识别，具体1395种类型可参见[列表](https://gitee.com/xudan16/wasm-tf-app/blob/master/mushroom-identification-js/src/tflite/label-mushroom.txt)。

## 2 运行APP

本APP已上线EdgeGallery能力中心，用户可以在EG平台实例化该APP，之后就可以通过Restful API方式体验该APP。

同时本APP提供`html`目录，里面是预先写好的nodejs以及html网页文件，可以用来调用`pkg`下生成的wasm文件提供AI网页服务。

```
http://<ip-of-host>:<port-of-app>
```
