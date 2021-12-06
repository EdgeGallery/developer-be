# 农作物病虫害识别

## 1 介绍

本APP可以根据农作物病虫害图片信息，对其进行病虫害类型检测。

本APP模型为[Tensorflow Lite模型](https://hub.tensorflow.google.cn/rishit-dagli/lite-model/plant-disease/default/1)，
来源于[TensorFlow Hub开源模型库](https://hub.tensorflow.google.cn/)。

本模型可以对38种类型的农作物病虫害进行检测识别，具体38种类型如下：

1. Apple_scab 苹果黑星病
2. Apple_Black_rot 苹果黑腐病
3. Apple_Cedar_apple_rust 苹果雪松锈病
4. Apple_healthy 健康苹果
5. Blueberry_healthy 健康蓝莓
6. Cherry_healthy 健康樱桃
7. Cherry_Powdery_mildew 樱桃白粉病
8. Corn_Cercospora_leaf_spot Gray_leaf_spot 玉米褐斑病 灰斑病
9. Corn_Common_rust 玉米常见锈病
10. Corn_healthy 健康玉米
11. Corn_Northern_Leaf_Blight 玉米北叶枯病
12. Grape_Black_rot 葡萄黑腐病
13. Grape_Esca_(Black_Measles) 葡萄黑麻疹
14. Grape_healthy 健康葡萄
15. Grape_Leaf_blight_(Isariopsis_Leaf_Spot) 葡萄叶枯病 叶斑病
16. Orange_Haunglongbing_(Citrus_greening) 柑橘青果病
17. Peach_Bacterial_spot 桃细菌性穿孔病
18. Peach_healthy 健康桃子
19. Pepper_bell_Bacterial_spot 辣椒细菌性斑点
20. Pepper_bell_healthy 健康辣椒
21. Potato_Early_blight 马铃薯早疫病
22. Potato_healthy 健康马铃薯
23. Potato_Late_blight 马铃薯晚疫病
24. Raspberry_healthy 健康覆盆子
25. Soybean_healthy 健康大豆
26. Squash_Powdery_mildew 南瓜白粉病
27. Strawberry_healthy 健康草莓
28. Strawberry_Leaf_scorch 草莓叶枯病
29. Tomato_Bacterial_spot 番茄细菌性斑点
30. Tomato_Early_blight 番茄枯萎病
31. Tomato_healthy 健康番茄
32. Tomato_Late_blight 番茄晚疫病
33. Tomato_Leaf_Mold 番茄叶霉病
34. Tomato_Septoria_leaf_spot 番茄斑叶病
35. Tomato_Spider_mites Two-spotted_spider_mite 番茄害螨
36. Tomato_Target_Spot 番茄靶斑病
37. Tomato_Tomato_mosaic_virus 番茄花叶病毒
38. Tomato_Tomato_Yellow_Leaf_Curl_Virus 番茄黄曲叶病毒

## 2 运行APP

本APP已上线EdgeGallery能力中心，用户可以在EG平台实例化该APP，之后就可以通过Restful API方式体验该APP。

同时本APP提供`html`目录，里面是预先写好的nodejs以及html网页文件，可以用来调用`pkg`下生成的wasm文件提供AI网页服务。

```
http://<ip-of-host>:<port-of-app>
```
