
#!/bin/bash


FLEDGE_HOST='http://0.0.0.0:32681'
KUIPER_HOST='http://0.0.0.0:32686'
MQTT_TDENG_ADAPTER_HOST='http://0.0.0.0:32687'
MQTT_BROKER_HOST='mqtt-broker'
BROKER_PORT='1883'


FLEDGE_SVC_URL="fledge/service"

### Install plugin mqtt-readngs
echo -e "\n**********Installing MQTT-Readings Plugin ***********\n"
curl -X POST -H "Content-Type: application/json" \
   -d '{"format":"repository","name":"fledge-south-mqtt-readings","version":""}'\
    $FLEDGE_HOST/fledge/plugins

sleep 10s
echo -e "\n*********** Config MQTT South device ***********\n"
### Subscribe south mqtt service to certain broker
curl -X POST -H "Content-Type: Application/json" -d '{"name": "in", "type": "south", "plugin" :"mqtt-readings","enabled":true,"config": {"brokerHost": {"value": "'"$MQTT_BROKER_HOST"'"},"brokerPort": {"value": "'"$BROKER_PORT"'"},"topic": {"value": "Room1/conditions"},"assetName":{"value":"mqtt-123"}}}' $FLEDGE_HOST/$FLEDGE_SVC_URL

sleep 10s
# Subscribe North mqtt service to certain broker
echo -e "\n*********** Config North Mqtt Interface ***********\n"
curl -X POST -H "Content-Type: Application/json" -d '{"name":"out", "plugin":"mqtt_north", "type":"north", "enabled":true, "config": {"brokerHost": {"value": "'"$MQTT_BROKER_HOST"'"},"brokerPort": {"value": "'"$BROKER_PORT"'"},"topic": {"value": "kuiper"}}}' $FLEDGE_HOST/fledge/service

sleep 10s


#### API to add streams with zmq
echo -e "\n********** Config Input Stream in Kuiper **************\n"
curl -X POST -H "Content-Type: Application/json" -d '{"sql":"create stream events () WITH ( datasource = \"kuiper\", FORMAT = \"json\")"}'\
        $KUIPER_HOST/streams

#### API to add rules with zmqi
echo -e "\n********** Config Rule in Kuiper **************\n"
curl -X POST -H "Content-Type: Application/json" -d '{"id": "rule2","sql": "SELECT asset, `timestamp`, round(readings->temp) AS temperature FROM events","actions": [{"mqtt": {"server": "tcp://mqtt-broker:1883","topic": "TDen/Round"}}]}' \
        $KUIPER_HOST/rules


echo -e "\n********** Config TDEngine Adapter **************\n"
curl --location --request POST $MQTT_TDENG_ADAPTER_HOST/api/v1/resource \
--header 'Content-Type: application/json' \
--data-raw '{
    "brokerAddr": {
        "host": "mqtt-broker",
        "port": 1883
    },
    "dbAddr": {
        "host": "td-engine",
        "port": 6030
    },
    "dbName": "iotdb",
    "store": [
        {
            "topic": "TDen/Round",
            "sTable": "meters",
            "tableNameJqPath": ".asset",
            "dataMapping": [
                {
                    "field": "ts",
                    "jqPath": ".timestamp",
                    "dataType": "TIMESTAMP"
                },
                {
                    "field": "temperature",
                    "jqPath": ".temperature",
                    "dataType": "TINYINT"
                }
            ]
        }
    ]
}'