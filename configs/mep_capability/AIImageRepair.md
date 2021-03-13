 Hi fill
==============
Function: Repairs ultra-HD images.

Reference: https://gitee.com/ascend/samples/tree/master/python/level2_simple_inference/6_other/imageinpainting_hifill

URL
```
POST /ascend/v1/hifill/input
```

Request parameters:

None

Body parameters:

| Name          | Type                        | Description              | Required      |
| ------------- | --------------------------- | ------------------------ | ------------- |
| Image    | file                      | Jpeg image file      | Yes |
| Left | 	string    | integer value in text  | Yes |
| Top | 	string    | integer value in text  | Yes |
| Width | 	string    | integer value in text  | Yes |
| Height | 	string    | integer value in text  | Yes |

Example Request:

```
POST http://{{MEP_IP}}:{{PORT}}/ascend/v1/hifill/input
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
GET /ascend/v1/hifill/output/{outputId}
```

Request parameters:

None

Body parameters:

None

Example Request:

```
GET http://{{MEP_IP}}:{{PORT}}/ascend/v1/hifill/output/e0d7058c8080345569679df0a172a9cd
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
