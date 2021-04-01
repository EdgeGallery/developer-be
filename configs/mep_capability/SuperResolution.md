Make a super Resoultion Image
==============
Function: Use one of the four models of SRCNN, FSRCNN, VDSR and ESPCN to perform image super-resolution processing on the input picture.

Reference: https://gitee.com/ascend/samples/tree/master/cplusplus/contrib/super_resolution

URL
```
POST /ascend/v1/superResolution/input
```

Request parameters:

None

Body parameters:

| Name          | Type                        | Description              | Required      |
| ------------- | --------------------------- | ------------------------ | ------------- |
| image    | file                      | bmp image file(.bmp)   | Yes |

Example Request:

```
POST http://{{MEP_IP}}:{{PORT}}/ascend/v1/superResolution/input
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
GET /ascend/v1/superResolution/output/{outputId}
```

Request parameters:

None

Body parameters:

None

Example Request:

```
GET http://{{MEP_IP}}:{{PORT}}/ascend/v1/superResolution/output/e0d7058c8080345569679df0a172a9cd
```

Return Parameters:

| Name          | Type                        | Description              |
| ------------- | --------------------------- | ------------------------ |
| file     | file                     | output file  in png format              |

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
