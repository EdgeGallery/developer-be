 Cartoon Image generation
==============

Function: Cartoonizes an input image by using the CartoonGAN model.

https://gitee.com/ascend/samples/tree/master/python/contrib/cartoonGAN_picture

URL
```
POST /ascend/v1/cartoon/input
```

Request parameters:

None

Body parameters:

| Name          | Type                        | Description              | Required      |
| ------------- | --------------------------- | ------------------------ | ------------- |
| Image    | file                      | Jpeg image file  in black and white    | Yes |

Example Request:

```
POST http://{{MEP_IP}}:{{PORT}}/ascend/v1/cartoon/input
```

Return Parameters:

| Name          | Type                        | Description              |
| ------------- | --------------------------- | ------------------------ |
| outputId     | string                     | Output Id to get the file later                  |

Return Code: 200 OK

Example Response:
```
HTTP/1.1 200 OK
{
    "outputId": "e0d7058c8080345569679df0a172a9cd"
}
```

Exception status code

| HTTP Status Code | Description |
| --- | --- |
| 400  | Bad request, used to indicate that the requested parameters are incorrect. |

URL

```
GET /ascend/v1/cartoon/output/{outputId}
```

Request parameters:

None

Body parameters:

None

Example Request:

```
GET http://{{MEP_IP}}:{{PORT}}/ascend/v1/cartoon/output/e0d7058c8080345569679df0a172a9cd
```

Return Parameters:

| Name          | Type                        | Description              |
| ------------- | --------------------------- | ------------------------ |
| file     | file                     | output file                |

Return Code: 200 OK

Example Response:
```
HTTP/1.1 200 OK
<<File output>>
```
Exception status code

| HTTP Status Code | Description |
| --- | --- |
| 404  | resource not found. |