#!/usr/bin/env bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
  CREATE DATABASE auth_user;
  CREATE DATABASE user_service;
  CREATE DATABASE tasks_service;
  CREATE DATABASE projects_service;
  CREATE DATABASE search_service;
  CREATE DATABASE dashboards_service;
EOSQL
