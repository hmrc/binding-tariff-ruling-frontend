#!/usr/bin/env bash
sbt scalafmtAll scalastyleAll compile coverage test coverageOff coverageReport dependencyUpdates
