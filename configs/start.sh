#!/bin/bash
# Copyright 2020 Huawei Technologies Co., Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Contain at most 63 characters
# Contain only lowercase alphanumeric characters or '-'
# Start with an alphanumeric character
# End with an alphanumeric character
validate_host_name()
{
  hostname="$1"
  len="${#hostname}"
  if [ "${len}" -gt "253" ] ; then
    return 1
  fi
  if ! echo "$hostname" | grep -qE '^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])(\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]{0,61}[a-zA-Z0-9]))*$' ; then
    return 1
  fi
  return 0
}

validate_url()
{
  url="$1"
  if ! echo "$url" | grep -qE '(https?|http)://[-A-Za-z0-9\+&@#/%?=~_|!:,.;]*[-A-Za-z0-9\+&@#/%=~_|]' ; then
    echo "invalid url"
    return 1
  fi
  return 0
}

validate_name()
{
  hostname="$1"
  len="${#hostname}"
  if [ "${len}" -gt "64" ] ; then
    return 1
  fi
  if ! echo "$hostname" | grep -qE '^[a-zA-Z0-9]*$|^[a-zA-Z0-9][a-zA-Z0-9_\-]*[a-zA-Z0-9]$' ; then
    return 1
  fi
  return 0
}

# Validating if port is > 1 and < 65535 , not validating reserved port.
validate_port_num()
{
  portnum="$1"
  len="${#portnum}"
  if [ "${len}" -gt "5" ] ; then
    return 1
  fi
  if ! echo "$portnum" | grep -qE '^-?[0-9]+$' ; then
    return 1
  fi
  if [ "$portnum" -gt "65535" ] || [ "$portnum" -lt "1" ] ; then
    return 1
  fi
  return 0
}

# Validating password.
# 1. password length should be more than 8 and less than 16
# 2. password must contain at least two types of the either one lowercase " +
#		 "character, one uppercase character, one digit or one special character
validate_password()
{
  password="$1"
  len="${#password}"
  if [ "${len}" -gt "16" ] || [ "${len}" -lt "8" ] ; then
    echo "password must not be less than 8 characters and more than 16 characters"
    return 1
  fi

  count=0
  if echo "$password" | grep -qE '[A-Z]' ; then
    count=$((count+1))
  fi

  if echo "$password" | grep -qE '[a-z]' ; then
    count=$((count+1))
  fi

  if echo "$password" | grep -qE '[0-9]' ; then
    count=$((count+1))
  fi

  if echo "$password" | grep -qE "[@#$%^'&'-+='('')']" ; then
    count=$((count+1))
  fi

  if [ "${count}" -lt "2" ] ; then
    echo "password must have atleast one uppercase character, lowercase character, digit or special character"
    return 1
  fi

  return 0
}

# validates whether file exist
validate_file_exists()
{
  file_path="$1"

  # checks variable is unset
  if [ -z "$file_path" ] ; then
    echo "file path variable is not set"
    return 1
  fi

   # checks if file exists
  if [ ! -f "$file_path" ] ; then
    echo "file does not exist"
    return 1
  fi

  return 0
}

# Validates if dir exists
validate_dir_exists()
{
  dir_path="$1"

  # checks if dir path var is unset
  if [ -z "$dir_path" ] ; then
    echo "dir path variable is not set"
    return 1
  fi

  # checks if dir exists
  if [ ! -d "$dir_path" ] ; then
    echo "dir does not exist"
    return 1
  fi

  return 0
}

# Validates if boolean is valid
validate_bool()
{
  env_var="$1"

  if  ! echo "$env_var" | grep -qx 'true'  &&  ! echo "$env_var" | grep -qx 'false' ; then
    echo "invalid boolean value"
    return 1
  fi

  return 0
}

# Validates if ip is valid
validate_ip()
{
  ip_var="$1"
  # checks if variable is unset
  if [ -z "$ip_var" ] ; then
    echo "ip is not set"
    return 1
  fi

  if ! echo "$ip_var" | grep -qE '^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.)' ; then
    return 1
  fi
  return 0
}

# ssl parameters validation
if [ ! -z "$SSL_ENABLED" ] ; then
  validate_bool "$SSL_ENABLED"
  valid_ssl_enabled="$?"
  if [ ! "$valid_ssl_enabled" -eq "0" ] ; then
    echo "invalid ssl enabled"
    exit 1
  fi
fi

if [ ! -z "$SSL_KEY_STORE_PATH" ] ; then
  validate_file_exists "$SSL_KEY_STORE_PATH"
  valid_ssl_key_store_path="$?"
  if [ ! "$valid_ssl_key_store_path" -eq "0" ] ; then
    echo "invalid ssl key store path"
    exit 1
  fi
fi

if [ ! -z "$SSL_KEY_STORE_PASSWORD" ] ; then
  validate_password "$SSL_KEY_STORE_PASSWORD"
  valid_ssl_key_store_password="$?"
  if [ ! "$valid_ssl_key_store_password" -eq "0" ] ; then
    echo "invalid ssl key store password, complexity validation failed"
    exit 1
  fi
fi

if [ ! -z "$SSL_KEY_STORE_TYPE" ] ; then
   validate_name "$SSL_KEY_STORE_TYPE"
   valid_name="$?"
   if [ ! "$valid_name" -eq "0" ] ; then
      echo "invalid ssl key store type"
      exit 1
   fi
fi

if [ ! -z "$SSL_KEY_ALIAS" ] ; then
   validate_name "$SSL_KEY_ALIAS"
   valid_name="$?"
   if [ ! "$valid_name" -eq "0" ] ; then
      echo "invalid ssl key alias"
      exit 1
   fi
fi

# db parameters validation
if [ ! -z "$POSTGRES_IP" ] ; then
   validate_host_name "$POSTGRES_IP"
   valid_db_host_name="$?"
   if [ ! "$valid_db_host_name" -eq "0" ] ; then
      echo "invalid db host name"
      exit 1
   fi
fi

if [ ! -z "$POSTGRES_PORT" ] ; then
   validate_port_num "$POSTGRES_PORT"
   valid_developer_db_port="$?"
   if [ ! "valid_developer_db_port" -eq "0" ] ; then
      echo "invalid developer db port number"
      exit 1
   fi
fi

if [ ! -z "$POSTGRES_DB_NAME" ] ; then
   validate_name "$POSTGRES_DB_NAME"
   valid_name="$?"
   if [ ! "$valid_name" -eq "0" ] ; then
      echo "invalid DB name"
      exit 1
   fi
fi

validate_name "$POSTGRES_USERNAME"
valid_name="$?"
if [ ! "$valid_name" -eq "0" ] ; then
  echo "invalid DB user name"
  exit 1
fi

validate_password "$POSTGRES_PASSWORD"
valid_developerdb_password="$?"
if [ ! "valid_developerdb_password" -eq "0" ] ; then
   echo "invalid developerdb password, complexity validation failed"
   exit 1
fi

# app parameters validation
if [ ! -z "$SC_ADDRESS" ] ; then
  validate_url "$SC_ADDRESS"
  valid_sc_address_host_name="$?"
  if [ ! "$valid_sc_address_host_name" -eq "0" ] ; then
    echo "invalid sc server host name"
     exit 1
  fi
fi

if [ ! -z "$AUTH_SERVER_ADDRESS" ] ; then
  validate_url "$AUTH_SERVER_ADDRESS"
  valid_auth_server_host_name="$?"
  if [ ! "$valid_auth_server_host_name" -eq "0" ] ; then
    echo "invalid auth server host name"
     exit 1
  fi
fi

if [ ! -z "$LOG_DIR" ] ; then
  validate_dir_exists "$LOG_DIR"
  valid_log_dir="$?"
  if [ ! "$valid_log_dir" -eq "0" ] ; then
    echo "log directory does not exist"
    exit 1
  fi
fi

validate_ip "$LISTEN_IP"
valid_listen_ip="$?"
if [ ! "$valid_listen_ip" -eq "0" ]; then
  echo "invalid ip address for listen ip"
  exit 1
fi

echo "Running Developer"
umask 0027
cd /usr/app || exit
java -jar bin/mec-developer-platform.jar