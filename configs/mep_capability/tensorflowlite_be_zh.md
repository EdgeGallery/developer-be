# 图片分类
## 功能介绍
基于Tensorflow Lite的图片分类
## Restful请求
```
POST /tensorflowlite/v1/image/action/classification
```
### 请求参数
请求Header：
```
  Content-Type:application/json
```
请求Body：
```
{
  "name": "图片名称",
  "image": "图片的Base64编码数据",
  "format": "图片的格式(png,jpeg,...)"
}
```
### 响应参数
响应Header：
```
  Content-Type:application/json
```
响应Body：
```
[{
  "label":"分类标签",
  "score":"分类得分"
},
...
]
```
### 样例
```
curl -X POST -d @request.json http://{hostIp}:33333/tensorflowlite/v1/image/action/classification
```
---
# 姿态检测
## 功能介绍
基于Tensorflow Lite的姿态检测,输出17个KeyPoint
## Restful请求
```
POST /tensorflowlite/v1/pose/action/movenet
```
### 请求参数
请求Header：
```
  Content-Type:application/json
```
请求Body：
```
{
  "name": "图片名称",
  "image": "图片的Base64编码数据",
  "format": "图片的格式(png,jpeg,...)"
}
```
### 响应参数
响应Header：
```
  Content-Type:application/json
```
响应Body：
```
{
  "nose": {
    "score": "鼻子KeyPoint得分",
    "x": "鼻子KeyPoint X坐标",
    "y": "鼻子KeyPoint Y坐标"
  },
  "leftEye": {
    "score": "左眼KeyPoint得分",
    "x": "左眼KeyPoint X坐标",
    "y": "左眼KeyPoint Y坐标"
  },
  "rightEye": {
    "score": "右眼KeyPoint得分",
    "x": "右眼KeyPoint X坐标",
    "y": "右眼KeyPoint Y坐标"
  },
  "leftEar": {
    "score": "左耳KeyPoint得分",
    "x": "左耳KeyPoint X坐标",
    "y": "左耳KeyPoint Y坐标"
  },
  "rightEar": {
    "score": "右耳KeyPoint得分",
    "x": "右耳KeyPoint X坐标",
    "y": "右耳KeyPoint Y坐标"
  },
  "leftShoulder": {
    "score": "左肩KeyPoint得分",
    "x": "左肩KeyPoint X坐标",
    "y": "左肩KeyPoint Y坐标"
  },
  "rightShoulder": {
    "score": "右肩KeyPoint得分",
    "x": "右肩KeyPoint X坐标",
    "y": "右肩KeyPoint Y坐标"
  },
  "leftElbow": {
    "score": "左眉KeyPoint得分",
    "x": "左眉KeyPoint X坐标",
    "y": "左眉KeyPoint Y坐标"
  },
  "rightElbow": {
    "score": "右眉KeyPoint得分",
    "x": "右眉KeyPoint X坐标",
    "y": "右眉KeyPoint Y坐标"
  },
  "leftWrist": {
    "score": "左腕KeyPoint得分",
    "x": "左腕KeyPoint X坐标",
    "y": "左腕KeyPoint Y坐标"
  },
  "rightWrist": {
    "score": "右腕KeyPoint得分",
    "x": "右腕KeyPoint X坐标",
    "y": "右腕KeyPoint Y坐标"
  },
  "leftHip": {
    "score": "左臀KeyPoint得分",
    "x": "左臀KeyPoint X坐标",
    "y": "左臀KeyPoint Y坐标"
  },
  "rightHip": {
    "score": "右臀KeyPoint得分",
    "x": "右臀KeyPoint X坐标",
    "y": "右臀KeyPoint Y坐标"
  },
  "leftKnee": {
    "score": "左膝KeyPoint得分",
    "x": "左膝KeyPoint X坐标",
    "y": "左膝KeyPoint Y坐标"
  },
  "rightKnee": {
    "score": "右膝KeyPoint得分",
    "x": "右膝KeyPoint X坐标",
    "y": "右膝KeyPoint Y坐标"
  },
  "leftAnkle": {
    "score": "左踝KeyPoint得分",
    "x": "左踝KeyPoint X坐标",
    "y": "左踝KeyPoint Y坐标"
  },
  "rightAnkle": {
    "score": "右踝KeyPoint得分",
    "x": "右踝KeyPoint X坐标",
    "y": "右踝KeyPoint Y坐标"
  }
}
```
### 样例
```
curl -X POST -d @request.json http://{hostIp}:33333/tensorflowlite/v1/pose/action/movenet
```
---
# 对象检测
## 功能介绍
基于Tensorflow Lite的对象检测，输出图片分类与Box位置
## Restful请求
```
POST /tensorflowlite/v1/image/action/object-detection
```
### 请求参数
请求Header：
```
  Content-Type:application/json
```
请求Body：
```
{
  "name": "图片名称",
  "image": "图片的Base64编码数据",
  "format": "图片的格式(png,jpeg,...)"
}
```
### 响应参数
响应Header：
```
  Content-Type:application/json
```
响应Body：
```
{
  "score": "得分",
  "label": "图片分类",
  "box": {
    "top": "矩形框顶部距离图片上部距离，单位为像素",
    "left": "矩形框左边距离图片左边距离，单位为像素",
    "bottom": "矩形框底部距离图片下部距离，单位为像素",
    "right": "矩形框右边距离图片右边距离，单位为像素"
  }
}
```
### 样例
```
curl -X POST -d @request.json http://{hostIp}:33333/tensorflowlite/v1/image/action/object-detection
```
---
# 图片风格化
## 功能介绍
基于Tensorflow Lite的图片风格化，根据源图片与风格图片生成具有风格图片样式的图片
## Restful请求
```
POST /tensorflowlite/v1/image/action/style-transfer
```
### 请求参数
请求Header：
```
  Content-Type:application/json
```
请求Body：
```
{
  "style": {
    "name": "风格图片名称",
    "image": "风格图片的Base64编码数据",
    "format": "风格图片的格式(png,jpeg,...)"
  },
  "content": {
    "name":  "源图片名称",
    "image": "源图片的Base64编码数据",
    "format": "源图片的格式(png,jpeg,...)"
  }
}
```
### 响应参数
响应Header：
```
  Content-Type:application/json
```
响应Body：
```
{
  "image": "图片的Base64编码数据",
  "format": "图片的格式(png,jpeg,...)"
}
```
### 样例
```
curl -X POST -d @request.json http://{hostIp}:33333/tensorflowlite/v1/image/action/style-transfer
```
---
# 超分辨率
## 功能介绍
基于Tensorflow Lite的超分辨率
## Restful请求
```
POST /tensorflowlite/v1/image/action/super-resolution
```
### 请求参数
请求Header：
```
  Content-Type:application/json
```
请求Body：
```
{
  "name": "图片名称",
  "image": "图片的Base64编码数据",
  "format": "图片的格式(png,jpeg,...)"
}
```
### 响应参数
响应Header：
```
  Content-Type:application/json
```
响应Body：
```
{
  "image": "超分辨率图片的Base64编码数据",
  "format": "超分辨率图片的格式(png,jpeg,...)"
}
```
### 样例
```
curl -X POST -d @request.json http://{hostIp}:33333/tensorflowlite/v1/image/action/super-resolution
```
