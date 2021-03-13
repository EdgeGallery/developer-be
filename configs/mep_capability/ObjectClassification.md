 Classification
==============

Function: Decoding the input jpeg picture, using the Resnet-50 model for classification reasoning after scaling.

Reference: https://ascend.huawei.com/en/#/software/mindx-sdk/applicationDetails/60172215?fromPage=1

URL
```
POST /ascend/v1/inferClassification/input
```

Request parameters:

None

Body parameters:

| Name          | Type                        | Description              | Required      |
| ------------- | --------------------------- | ------------------------ | ------------- |
| Image    | file                      | Jpeg image file      | Yes |

Example Request:

```
POST http://{{MEP_IP}}:{{PORT}}/ascend/v1/inferClassification/input
```

Return Parameters:

| Name          | Type                        | Description              |
| ------------- | --------------------------- | ------------------------ |
| result     | string                     | result of inference                 |

Return Code: 200 OK

Example Response:
```
HTTP/1.1 200 OK
{
    "result": "classname:  248: 'Eskimo dog, husky',\n"
}
```

Exception status code

| HTTP Status Code | Description |
| --- | --- |
| 400  | Bad request, used to indicate that the requested parameters are incorrect. |