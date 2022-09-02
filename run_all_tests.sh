#!/usr/bin/env bash
sbt scalafmtAll scalastyle compile coverage test coverageOff coverageReport dependencyUpdates
