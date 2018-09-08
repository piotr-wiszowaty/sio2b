#!/bin/sh
foco65 -p '$3000' setup.forth > /tmp/setup.asx || exit 1
xasm /l /tmp/setup.asx || exit 1
