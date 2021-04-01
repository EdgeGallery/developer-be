Crop An Image
==============
Function: Extracting the specified part from the input yuv format picture and saving it locally.

Reference: https://ascend.huawei.com/en/#/software/mindx-sdk/applicationDetails/60172215?fromPage=1

URL
```
POST /ascend/v1/cropPic/input
```

Request parameters:

None

Body parameters:

| Name          | Type                        | Description              | Required      |
| ------------- | --------------------------- | ------------------------ | ------------- |
| image    | file                      | Jpeg image file      | Yes |
| left | 	string    | integer value in text  | Yes |
| top | 	string    | integer value in text  | Yes |
| width | 	string    | integer value in text  | Yes |
| height | 	string    | integer value in text  | Yes |

Example Request:

```
POST http://{{MEP_IP}}:{{PORT}}/ascend/v1/cropPic/input
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
GET /ascend/v1/cropPic/output/{outputId}
```

Request parameters:

None

Body parameters:

None

Example Request:

```
GET http://{{MEP_IP}}:{{PORT}}/ascend/v1/cropPic/output/e0d7058c8080345569679df0a172a9cd
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