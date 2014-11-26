#!/bin/bash
echo $2
echo $1
java -cp "./Agonizer/deps/*:out/production/Agonizer/" hps.nyu.fa14.Main $2 < $1
