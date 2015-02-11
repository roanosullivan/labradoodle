#!/bin/sh

PROJ_NAME=$1
lein exec -pe "(use 'labradoodle.core)(export-punchlist \"${PROJ_NAME}\")"
